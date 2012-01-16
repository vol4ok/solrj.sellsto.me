package org.apache.solr.schema;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrConstantScoreQuery;
import org.apache.solr.search.function.ValueSourceRangeFilter;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Field type for support of monetary values.
 * <p>
 * See <a href="http://wiki.apache.org/solr/MoneyFieldType">http://wiki.apache.org/solr/MoneyFieldType</a>
 */
public class MoneyType extends org.apache.solr.schema.FieldType implements org.apache.solr.schema.SchemaAware, ResourceLoaderAware {
    private IndexSchema schema;
    private String currencyConfigPath;
    private String defaultCurrency;
    private AtomicReference<CurrencyConfig> currencyConfigRef = new AtomicReference<CurrencyConfig>();
    public static Logger log = LoggerFactory.getLogger(MoneyType.class);

    private CurrencyConfig currencyConfig() {
        return currencyConfigRef.get();
    }

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        this.schema = schema;
        this.currencyConfigPath = args.get("currencyConfig");
        this.defaultCurrency = args.get("defaultCurrency");

        if (this.defaultCurrency == null) {
            this.defaultCurrency = "USD";
        }

        if (java.util.Currency.getInstance(this.defaultCurrency) == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid currency code " + this.defaultCurrency);
        }

        args.remove("currencyConfig");
        super.init(schema, args);
    }

    /**
     * Reloads the currency config file specified by the schema.
     * <p/>
     * This should be called whenever the currency configuration file changes.
     *
     * @param loader The resource loader.
     */
    public void reloadCurrencyConfig(ResourceLoader loader) {
        InputStream is;
        log.info("Reloading currency configuration file at " + currencyConfigPath);

        try {
            is = loader.openResource(currencyConfigPath);
        } catch (IOException e) {
            log.error("Error reloading currency configuration file at " + currencyConfigPath, e);
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
        }

        try {
            CurrencyConfig newConfig = CurrencyConfig.readConfig(is);
            log.info("Read new currency configuration " + newConfig);
            currencyConfigRef.set(newConfig);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public boolean isPolyField() {
        return true;
    }

    @Override
    public IndexableField[] createFields(SchemaField field, Object externalVal, float boost) {
        MoneyValue value = MoneyValue.parse(externalVal.toString(), defaultCurrency);

        IndexableField[] f = new IndexableField[field.stored() ? 3 : 2];
        f[0] = getAmountField(field).createField(String.valueOf(value.getAmount()), boost);
        f[1] = getCurrencyField(field).createField(value.getCurrencyCode(), boost);

        if (field.stored()) {
            FieldType customType = new FieldType();
            customType.setStored(true);
            f[2] = createField(field.getName(), externalVal.toString(), customType, boost);
        }

        return f;
    }

    private SchemaField getAmountField(SchemaField field) {
        return schema.getField(field.getName() + POLY_FIELD_SEPARATOR + "_amount_raw");
    }

    private SchemaField getCurrencyField(SchemaField field) {
        return schema.getField(field.getName() + POLY_FIELD_SEPARATOR + "_currency");
    }

    private void createDynamicMoneyField(String suffix, String fieldType) {
        String name = "*" + POLY_FIELD_SEPARATOR + suffix;
        Map<String, String> props = new HashMap<String, String>();
        props.put("indexed", "true");
        props.put("stored", "false");
        props.put("multiValued", "false");
        org.apache.solr.schema.FieldType type = schema.getFieldTypeByName(fieldType);
        int p = SchemaField.calcProps(name, type, props);
        schema.registerDynamicField(SchemaField.create(name, type, p, null));
    }

    /**
     * When index schema is informed, add dynamic fields.
     *
     * @param indexSchema The index schema.
     */
    public void inform(IndexSchema indexSchema) {
        createDynamicMoneyField("_currency", "string");
        createDynamicMoneyField("_amount_raw", "long");
    }

    /**
     * Load the currency config when resource loader initialized.
     *
     * @param resourceLoader The resource loader.
     */
    public void inform(ResourceLoader resourceLoader) {
        reloadCurrencyConfig(resourceLoader);
    }

    @Override
    public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
        MoneyValue value = MoneyValue.parse(externalVal, defaultCurrency);
        MoneyValue valueDefault;
        valueDefault = value.convertTo(currencyConfig(), defaultCurrency);

        return getRangeQuery(parser, field, valueDefault.toString(), valueDefault.toString(), true, true);
    }

    @Override
    public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, final boolean minInclusive, final boolean maxInclusive) {
        final MoneyValue p1 = MoneyValue.parse(part1, defaultCurrency);
        final MoneyValue p2 = MoneyValue.parse(part2, defaultCurrency);

        if (!p1.getCurrencyCode().equals(p2.getCurrencyCode())) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Cannot parse range query " + part1 + " to " + part2 +
                            ": range queries only supported when upper and lower bound have same currency.");
        }

        String currencyCode = p1.getCurrencyCode();
        final MoneyValueSource vs = new MoneyValueSource(field, currencyCode, parser);

        return new SolrConstantScoreQuery(new ValueSourceRangeFilter(vs,
                p1.getAmount() + "", p2.getAmount() + "", minInclusive, maxInclusive));
    }

    @Override
    public SortField getSortField(SchemaField field, boolean reverse) {
        try {
            // Convert all values to default currency for sorting.
            return (new MoneyValueSource(field, defaultCurrency, null)).getSortField(reverse);
        } catch (IOException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
        }
    }

    public void write(XMLWriter xmlWriter, String name, IndexableField field) throws IOException {
        xmlWriter.writeStr(name, field.stringValue(), false);
    }

    @Override public void write(TextResponseWriter writer, String name, IndexableField field) throws IOException {
        writer.writeStr(name, field.stringValue(), false);
    }

    class MoneyValueSource extends ValueSource {
        private String targetCurrencyCode;
        private ValueSource currencyValues;
        private ValueSource amountValues;

        public MoneyValueSource(SchemaField sf, String targetCurrencyCode, QParser parser) {
            this.targetCurrencyCode = targetCurrencyCode;

            SchemaField amountField = schema.getField(sf.getName() + POLY_FIELD_SEPARATOR + "_amount_raw");
            SchemaField currencyField = schema.getField(sf.getName() + POLY_FIELD_SEPARATOR + "_currency");

            currencyValues = currencyField.getType().getValueSource(currencyField, parser);
            amountValues = amountField.getType().getValueSource(amountField, parser);
        }

        public FunctionValues getValues(Map context, IndexReader.AtomicReaderContext reader) throws IOException {
            final FunctionValues amounts = amountValues.getValues(context, reader);
            final FunctionValues currencies = currencyValues.getValues(context, reader);

            return new FunctionValues() {
                private final int MAX_CURRENCIES_TO_CACHE = 256;
                private final int[] fractionDigitCache = new int[MAX_CURRENCIES_TO_CACHE];
                private final String[] currencyOrdToCurrencyCache = new String[MAX_CURRENCIES_TO_CACHE];
                private final double[] exchangeRateCache = new double[MAX_CURRENCIES_TO_CACHE];
                private int targetFractionDigits = -1;
                private int targetCurrencyOrd = -1;
                private boolean initializedCache;

                private String getDocCurrencyCode(int doc, int currencyOrd) {
                    if (currencyOrd < MAX_CURRENCIES_TO_CACHE) {
                        String currency = currencyOrdToCurrencyCache[currencyOrd];

                        if (currency == null) {
                            currencyOrdToCurrencyCache[currencyOrd] = currency = currencies.strVal(doc);
                        }

                        if (targetCurrencyOrd == -1 && currency.equals(targetCurrencyCode)) {
                            targetCurrencyOrd = currencyOrd;
                        }

                        return currency;
                    } else {
                        return currencies.strVal(doc);
                    }
                }

                public long longVal(int doc) {
                    if (!initializedCache) {
                        for (int i = 0; i < fractionDigitCache.length; i++) {
                            fractionDigitCache[i] = -1;
                        }

                        initializedCache = true;
                    }

                    long amount = amounts.longVal(doc);
                    int currencyOrd = currencies.ordVal(doc);

                    if (currencyOrd == targetCurrencyOrd) {
                        return amount;
                    }

                    double exchangeRate;
                    int sourceFractionDigits;

                    if (targetFractionDigits == -1) {
                        targetFractionDigits = Currency.getInstance(targetCurrencyCode).getDefaultFractionDigits();
                    }

                    if (currencyOrd < MAX_CURRENCIES_TO_CACHE) {
                        exchangeRate = exchangeRateCache[currencyOrd];

                        if (exchangeRate <= 0.0) {
                            String sourceCurrencyCode = getDocCurrencyCode(doc, currencyOrd);
                            exchangeRate = exchangeRateCache[currencyOrd] = currencyConfig().getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
                        }

                        sourceFractionDigits = fractionDigitCache[currencyOrd];

                        if (sourceFractionDigits == -1) {
                            String sourceCurrencyCode = getDocCurrencyCode(doc, currencyOrd);
                            sourceFractionDigits = fractionDigitCache[currencyOrd] = Currency.getInstance(sourceCurrencyCode).getDefaultFractionDigits();
                        }
                    } else {
                        String sourceCurrencyCode = getDocCurrencyCode(doc, currencyOrd);
                        exchangeRate = currencyConfig().getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
                        sourceFractionDigits = Currency.getInstance(sourceCurrencyCode).getDefaultFractionDigits();
                    }

                    return MoneyValue.convertAmount(exchangeRate, sourceFractionDigits, amount, targetFractionDigits);
                }

                public int intVal(int doc) {
                    return (int) longVal(doc);
                }

                public double doubleVal(int doc) {
                    return (double) longVal(doc);
                }

                public float floatVal(int doc) {
                    return (float) longVal(doc);
                }

                public String strVal(int doc) {
                    return Long.toString(longVal(doc));
                }

                public String toString(int doc) {
                    return name() + '(' + amounts.toString(doc) + ',' + currencies.toString(doc) + ')';
                }
            };
        }

        public String name() {
            return "money";
        }

        @Override public String description() {
            return "money";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MoneyValueSource that = (MoneyValueSource) o;

            return !(amountValues != null ? !amountValues.equals(that.amountValues) : that.amountValues != null) &&
                    !(currencyValues != null ? !currencyValues.equals(that.currencyValues) : that.currencyValues != null) &&
                    !(targetCurrencyCode != null ? !targetCurrencyCode.equals(that.targetCurrencyCode) : that.targetCurrencyCode != null);

        }

        @Override
        public int hashCode() {
            int result = targetCurrencyCode != null ? targetCurrencyCode.hashCode() : 0;
            result = 31 * result + (currencyValues != null ? currencyValues.hashCode() : 0);
            result = 31 * result + (amountValues != null ? amountValues.hashCode() : 0);
            return result;
        }
    }
}

interface ExchangeRateProvider {
    public double getExchangeRate(String sourceCurrencyCode, String targetCurrencyCode);
}

/**
 * Configuration for currency. Provides currency exchange rates.
 */
class CurrencyConfig implements ExchangeRateProvider {
    public static Logger log = LoggerFactory.getLogger(CurrencyConfig.class);

    // Exchange rate map, maps Currency Code -> Currency Code -> Rate
    private Map<String, Map<String, Double>> rates = new HashMap<String, Map<String, Double>>();

    /**
     * Reads the currency configuration XML from the specified InputStream .
     *
     * @param is Stream to read configuration from.
     * @return The parsed and initialized CurrencyConfig.
     */
    public static CurrencyConfig readConfig(InputStream is) {
        javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setXIncludeAware(true);
            dbf.setNamespaceAware(true);
        } catch (UnsupportedOperationException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "XML parser doesn't support XInclude option", e);
        }

        try {
            Document doc = dbf.newDocumentBuilder().parse(is);
            CurrencyConfig config = new CurrencyConfig();
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            // Parse exchange rates.
            NodeList nodes = (NodeList) xpath.evaluate("/currencyConfig/rates/rate", doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node rateNode = nodes.item(i);
                NamedNodeMap attributes = rateNode.getAttributes();
                Node from = attributes.getNamedItem("from");
                Node to = attributes.getNamedItem("to");
                Node rate = attributes.getNamedItem("rate");

                if (from == null || to == null || rate == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Exchange rate missing attributes (required: from, to, rate) " + rateNode);
                }

                String fromCurrency = from.getNodeValue();
                String toCurrency = to.getNodeValue();
                Double exchangeRate;

                if (java.util.Currency.getInstance(fromCurrency) == null ||
                        java.util.Currency.getInstance(toCurrency) == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Could not find from currency specified in exchange rate: " + rateNode);
                }

                try {
                    exchangeRate = Double.parseDouble(rate.getNodeValue());
                } catch (NumberFormatException e) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Could not parse exchange rate: " + rateNode, e);
                }

                config.registerExchangeRate(fromCurrency, toCurrency, exchangeRate);
            }

            return config;
        } catch (SAXException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error parsing currency config.", e);
        } catch (IOException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error parsing currency config.", e);
        } catch (ParserConfigurationException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error parsing currency config.", e);
        } catch (XPathExpressionException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error parsing currency config.", e);
        }
    }

    /**
     * Returns the currently known exchange rate between two currencies. If a direct rate has been loaded,
     * it is used. Otherwise, if a rate is known to convert the target currency to the source, the inverse
     * exchange rate is computed.
     *
     * @param sourceCurrencyCode The source currency being converted from.
     * @param targetCurrencyCode The target currency being converted to.
     * @return The exchange rate.
     */
    public double getExchangeRate(String sourceCurrencyCode, String targetCurrencyCode) {
        if (sourceCurrencyCode.equals(targetCurrencyCode)) {
            return 1.0;
        }

        Double directRate = lookupRate(sourceCurrencyCode, targetCurrencyCode);

        if (directRate != null) {
            return directRate;
        }

        Double symmetricRate = lookupRate(targetCurrencyCode, sourceCurrencyCode);

        if (symmetricRate != null) {
            return 1.0 / symmetricRate;
        }

        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No available conversion rate between " + sourceCurrencyCode + " to " + targetCurrencyCode);
    }

    /**
     * Looks up the current known rate, if any, between the source and target currencies.
     *
     * @param sourceCurrencyCode The source currency being converted from.
     * @param targetCurrencyCode The target currency being converted to.
     * @return The exchange rate, or null if no rate has been registered.
     */
    private Double lookupRate(String sourceCurrencyCode, String targetCurrencyCode) {
        Map<String, Double> rhs = rates.get(sourceCurrencyCode);

        if (rhs != null) {
            return rhs.get(targetCurrencyCode);
        }

        return null;
    }

    /**
     * Registers the specified exchange rate.
     *
     * @param sourceCurrencyCode The source currency.
     * @param targetCurrencyCode The target currency.
     * @param rate               The known exchange rate.
     */
    private void registerExchangeRate(String sourceCurrencyCode, String targetCurrencyCode, double rate) {
        Map<String, Double> rhs = rates.get(sourceCurrencyCode);

        if (rhs == null) {
            rhs = new HashMap<String, Double>();
            rates.put(sourceCurrencyCode, rhs);
        }

        rhs.put(targetCurrencyCode, rate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyConfig that = (CurrencyConfig) o;

        return !(rates != null ? !rates.equals(that.rates) : that.rates != null);
    }

    @Override
    public int hashCode() {
        return rates != null ? rates.hashCode() : 0;
    }

    public String toString() {
        return "[CurrencyConfig : " + rates.size() + " rates.]";
    }
}

/**
 * Represents a Money field value, which includes a long amount and ISO currency code.
 */
class MoneyValue {
    private long amount;
    private String currencyCode;

    /**
     * Constructs a new money value.
     *
     * @param amount       The amount.
     * @param currencyCode The currency code.
     */
    public MoneyValue(long amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    /**
     * Constructs a new money value by parsing the specific input.
     * <p/>
     * Money values are expected to be in the format &lt;amount&gt;,&lt;currency code&gt;,
     * for example, "500,USD" would represent 5 U.S. Dollars.
     * <p/>
     * If no currency code is specified, the default is assumed.
     *
     * @param externalVal The value to parse.
     * @param defaultCurrency The default currency.
     * @return The parsed MoneyValue.
     */
    public static MoneyValue parse(String externalVal, String defaultCurrency) {
        String amount = externalVal;
        String code = defaultCurrency;

        if (externalVal.contains(",")) {
            String[] amountAndCode = externalVal.split(",");
            amount = amountAndCode[0];
            code = amountAndCode[1];
        }

        if (java.util.Currency.getInstance(code) == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid currency code " + code);
        }

        try {
            return new MoneyValue(Long.parseLong(amount), code);
        } catch (NumberFormatException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
        }
    }

    /**
     * The amount of the MoneyValue.
     *
     * @return The amount.
     */
    public long getAmount() {
        return amount;
    }

    /**
     * The ISO currency code of the MoneyValue.
     *
     * @return The currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Performs a currency conversion & unit conversion.
     *
     * @param exchangeRates      Exchange rates to apply.
     * @param sourceCurrencyCode The source currency code.
     * @param sourceAmount       The source amount.
     * @param targetCurrencyCode The target currency code.
     * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
     */
    public static long convertAmount(ExchangeRateProvider exchangeRates, String sourceCurrencyCode, long sourceAmount, String targetCurrencyCode) {
        double exchangeRate = exchangeRates.getExchangeRate(sourceCurrencyCode, targetCurrencyCode);
        return convertAmount(exchangeRate, sourceCurrencyCode, sourceAmount, targetCurrencyCode);
    }

    /**
     * Performs a currency conversion & unit conversion.
     *
     * @param exchangeRate         Exchange rate to apply.
     * @param sourceFractionDigits The fraction digits of the source.
     * @param sourceAmount         The source amount.
     * @param targetFractionDigits The fraction digits of the target.
     * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
     */
    public static long convertAmount(final double exchangeRate, final int sourceFractionDigits, final long sourceAmount, final int targetFractionDigits) {
        int digitDelta = targetFractionDigits - sourceFractionDigits;
        double value = ((double) sourceAmount * exchangeRate);

        if (digitDelta != 0) {
            if (digitDelta < 0) {
                for (int i = 0; i < -digitDelta; i++) {
                    value *= 0.1;
                }
            } else {
                for (int i = 0; i < digitDelta; i++) {
                    value *= 10.0;
                }
            }
        }

        return (long) value;
    }

    /**
     * Performs a currency conversion & unit conversion.
     *
     * @param exchangeRate       Exchange rate to apply.
     * @param sourceCurrencyCode The source currency code.
     * @param sourceAmount       The source amount.
     * @param targetCurrencyCode The target currency code.
     * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
     */
    public static long convertAmount(double exchangeRate, String sourceCurrencyCode, long sourceAmount, String targetCurrencyCode) {
        if (targetCurrencyCode.equals(sourceCurrencyCode)) {
            return sourceAmount;
        }

        int sourceFractionDigits = Currency.getInstance(sourceCurrencyCode).getDefaultFractionDigits();
        Currency targetCurrency = Currency.getInstance(targetCurrencyCode);
        int targetFractionDigits = targetCurrency.getDefaultFractionDigits();
        return convertAmount(exchangeRate, sourceFractionDigits, sourceAmount, targetFractionDigits);
    }

    /**
     * Returns a new MoneyValue that is the conversion of this MoneyValue to the specified currency.
     *
     * @param exchangeRates      The exchange rate provider.
     * @param targetCurrencyCode The target currency code to convert this MoneyValue to.
     * @return The converted MoneyValue.
     */
    public MoneyValue convertTo(ExchangeRateProvider exchangeRates, String targetCurrencyCode) {
        return new MoneyValue(convertAmount(exchangeRates, this.getCurrencyCode(), this.getAmount(), targetCurrencyCode), targetCurrencyCode);
    }

    public String toString() {
        return String.valueOf(amount) + "," + currencyCode;
    }
}
