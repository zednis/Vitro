* PubMed Import tools for Vitro/Vivo ==============================
Brian Caruso
2005-03-23
Mann Library, Cornell University.

This is a set of tools writen in java to download citations from the
National Library of Medicine and load them into the Vivo system.  The
two main components are the PMParser and the Fetcher.  

* Quick guide ==============================

** 1 Get records from pubmed:
$ ant makeRunScript
$ ./run.sh Fetcher Fetch.properties

** 2 Load those into vitro pubs and tokens tables:
$ ./run.sh PMParser pubmed.properties connection.properties pubmed.fetch.xml

** 3 Move from pubs and tokens tables to vivo entities:
$ cd ../biosis
$ ./run.sh edu.cornell.mannlib.vitro.biosis.UpdateAuthorTokensWithMatchingEntityId connection.properties
$ ./run.sh edu.cornell.mannlib.vitro.biosis.TransferPubsToEntities connection.properties


* Fetcher tool ==============================
Usage:
java Fetcher Fetch.properties

The Fetcher is configured by the Fetch.properties file and retrieves a
set of citations to a local file.  The properties file can be setup to
indicate the query string to use, the database to query, a time frame
for the citations and the file name to same to.  The citations are
gathered using a two step process.  First a pubmed web service is used
to search for a list of article ids that fit the query, second a
different service is used to gather the xml citations.  

The result of the first stage is an xml of the search result with the
id's of the relivant articles and key to the results on the pubmed
server.  This key can be used to refer to the results of the search in
later requests.  The values from the search results are extracted
using XPaths which are specified in the Fetch.properties file.

The actual fetching of the results is done in batches of 500.  The
query string is constructed from the Fetcher.fetchQuery by replacing
tags with the values needed for a given batch.  example:

Fetcher.fetchQuery=WebEnv=<<<<webenv&query_key=<<<<querykey&restart=<<<<start&retmax=<<<<retrymax

Here the <<<webenv will be replaced with the value of the web
environment from the search result.  <<<<querykey will be replaced with
the query key from the search result.  <<<<start will be replaced with
the number of the start of this block and <<<<retrymax will be replaced
with the number of citations to download in a block.


* The PMParser tool ==============================
Usage:
java PMParser pubmed.properties connection.properties citations.xml
Where pubmed.properties is a file that contains the configuration
strings for the parser and citations.xml is the xml that you want to
load.

The PMParser is a command line tool that will load citations into a
vivo system.  The PMParser is similar to the Fetcher in that it uses
XPaths in a properties file for configuration.  The PMParser is
configured using XPath expressions in the properties file to indicate
which nodes to stick into which parts of the Article object.  This
creates a flexible design where the parser could be reconfigured to
handle other xml schemas.  There are a couple of wrinkles that 
need to be ironed out.  These include the author lists, author names
and dates; all of which use code to control the parsing.  One solution
would be to use xstl to transform other date/author formats to the
pubmed/medline format.  These xstl strings could be stored in the
properties file.

* Improvement Ideas ==============================
The fetcher tool would be more useful in a shell scripting environment
if it could take the query string and the output file name from the 
command line.
