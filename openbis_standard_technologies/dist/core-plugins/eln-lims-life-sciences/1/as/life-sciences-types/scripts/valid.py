def validate(entity, isNew):
  if isNew:
    if not entity.properties() is None:
      return "It is not allowed to attach properties to new sample."