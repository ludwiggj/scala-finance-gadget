1.equals(None)
None.equals(2)
None.equals(None)

def optionEquals(val1: Option[BigDecimal], val2: Option[BigDecimal]) = {
  (val1.isEmpty && val2.isEmpty) ||
    (val1.isDefined && val2.isDefined && (val1.get == val2.get))
}

optionEquals(None, None)
optionEquals(None, Some(BigDecimal(1)))
optionEquals(Some(BigDecimal(2)), None)
optionEquals(Some(BigDecimal(2)), Some(BigDecimal(3)))
optionEquals(Some(BigDecimal(2)), Some(BigDecimal(2)))

BigDecimal(2)