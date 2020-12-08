import openbis from '@srcTest/js/services/openbis.js'

const testDatabase = new openbis.QueryDatabase()
testDatabase.setName('test database')
testDatabase.setPermId(new openbis.QueryDatabaseName(testDatabase.getName()))

const anotherDatabase = new openbis.QueryDatabase()
anotherDatabase.setName('another database')
anotherDatabase.setPermId(
  new openbis.QueryDatabaseName(anotherDatabase.getName())
)

const genericQuery = new openbis.Query()
genericQuery.setName('generic query')
genericQuery.setDescription('generic query description')
genericQuery.setDatabaseId(testDatabase.getPermId())
genericQuery.setQueryType(openbis.QueryType.GENERIC)
genericQuery.setSql('select * from persons;')
genericQuery.setPublic(true)

const sampleQuery = new openbis.Query()
sampleQuery.setName('sample query')
sampleQuery.setDescription('sample query description')
sampleQuery.setDatabaseId(anotherDatabase.getPermId())
sampleQuery.setQueryType(openbis.QueryType.SAMPLE)
sampleQuery.setSql('select * from samples;')
sampleQuery.setPublic(false)

const dataSetQuery = new openbis.Query()
dataSetQuery.setName('data set query')
dataSetQuery.setDescription('data set query description')
dataSetQuery.setDatabaseId(anotherDatabase.getPermId())
dataSetQuery.setQueryType(openbis.QueryType.DATA_SET)
dataSetQuery.setSql('select * from data;')
dataSetQuery.setPublic(true)

export default {
  testDatabase,
  anotherDatabase,
  genericQuery,
  sampleQuery,
  dataSetQuery
}
