package sellstome.lucene.codecs.values

/** Possible ways for resolving for doc slices generation */
protected object FindDVSlicesGenMethod extends Enumeration {
  /** types for enum constants */
  type FindDVSlicesGenMethod = Value

  val FileSystem, LookAhead = Value
}
