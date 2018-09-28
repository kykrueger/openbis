import sys
import unittest
import StringIO
from resync.resource import Resource
from resync.list_base import ListBase
from resync.sitemap import Sitemap, SitemapIndexError

# etree gives ParseError in 2.7, ExpatError in 2.6
etree_error_class = None
if (sys.version_info < (2,7)):
    from xml.parsers.expat import ExpatError
    etree_error_class = ExpatError
else:
    from xml.etree.ElementTree import ParseError
    etree_error_class = ParseError

class TestListBase(unittest.TestCase):

    def test_05_len_count(self):
        # count sets explicit number of resources, len(resources) not used
        lb = ListBase()
        self.assertEqual( len(lb), 0 )
        lb.add(Resource('a'))
        self.assertEqual( len(lb), 1 )
        lb = ListBase( count=100 )
        self.assertEqual( len(lb), 100 )

    def test_08_print(self):
        lb = ListBase()
        lb.add( Resource(uri='a',lastmod='2001-01-01',length=1234) )
        lb.add( Resource(uri='b',lastmod='2002-02-02',length=56789) )
        lb.add( Resource(uri='c',lastmod='2003-03-03',length=0) )
        lb.md['from']=None #avoid now being added
        #print lb
        self.assertEqual( lb.as_xml(), '<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/"><rs:md capability="unknown" /><url><loc>a</loc><lastmod>2001-01-01T00:00:00Z</lastmod><rs:md length="1234" /></url><url><loc>b</loc><lastmod>2002-02-02T00:00:00Z</lastmod><rs:md length="56789" /></url><url><loc>c</loc><lastmod>2003-03-03T00:00:00Z</lastmod><rs:md length="0" /></url></urlset>' )

    def test_11_parse_2(self):
        xml='<?xml version=\'1.0\' encoding=\'UTF-8\'?>\n\
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/">\
<rs:md capability="unknown" from="2013-02-12T14:09:00Z" />\
<url><loc>/tmp/rs_test/src/file_a</loc><lastmod>2012-03-14T18:37:36Z</lastmod><rs:md length="12" /></url>\
<url><loc>/tmp/rs_test/src/file_b</loc><lastmod>2012-03-14T18:37:36Z</lastmod><rs:md length="32" /></url>\
</urlset>'
        lb=ListBase()
        lb.parse(fh=StringIO.StringIO(xml))
        self.assertEqual( len(lb.resources), 2, 'got 2 resources')

if  __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(TestListBase)
    unittest.TextTestRunner(verbosity=2).run(suite)
