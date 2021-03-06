<head>
  <title>Quantitative Genomics Facility</title>
  <link rel="stylesheet" href="bootstrap3/css/bootstrap.min.css">
  <script type="text/javascript" src="d3.v2.min.js"></script>
  <script type="text/javascript" src="/openbis/resources/js/jquery.js"></script>
  <script type="text/javascript" src="bootstrap3/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="spin.min.js"></script>
  <script type="text/javascript" src="/openbis/resources/js/openbis.js"></script>
  <script type="text/javascript" src="openbis-dsu.js"></script>
  <!-- To speed development, cache the requests -->
  <!-- <script type="text/javascript" src="openbis-request-cache.js"></script> -->
  <script>

dsu = new openbis_dsu('/openbis/openbis', '/datastore_server');
var vis;
var didCreateVis = false;
var context = new openbisWebAppContext();

$(document).ready(function() {
});

function createVis()
{ 
	if (didCreateVis) return;
	vis = d3.select("#main").append("div").attr("id", "vis");
	didCreateVis = true;
}

function displayReturnedTable(data)
{
	if (data.error) {
		console.log(data.error);
		vis.append("p").text("Could not retrieve data.");
		return;
	}
	
	var dataToShow = data.result;

	d3.select("#progress").remove()
        d3.select("#button-group").remove()
	
	vis.append("p").text("");	
	// Pick all div elements of the visualization
	vis.selectAll("div")
           .data(dataToShow.rows)
	   .enter()
	   .append("div")
	   .html(function(row) { return row[1].value;})

	var button = d3.select("#container")
	               .append("div")

        button.selectAll("button")
            .data(dataToShow.rows)
            .enter()
            .append("div")
            .append("button")
            .attr("id", function(row) { return row[0].value; })
            .attr("class", function(row) { if (row[0].value == 0) {return "btn btn-success";} return "btn btn-danger";})
            //.attr("onclick", function(row) { return "callIngestionSetInvoice('" + row + "');" })
            .text(function(row) { if (row[0].value == 0) { return "OK"; } return "Fail";});

//        var invoices = [];
//        for (var i=0;i<data.result.rows.length;i++) { 
//           //var val = data.result.rows[i][1].value.split('#')[0]; 
//           var val = data.result.rows[i][1].value; 
//           if (val) {
//             invoices.push(val)
//           } 
//        }
}

function hideButtons(data)
{
        var buttonId = data.result.rows[0][0].value;
	d3.select("#setInvoice").remove()
        d3.select("#main").append("div").attr("id", "Done").append("p").text("Done " + buttonId);
        d3.select("button#" + String(buttonId)).remove();
}

function spinner (target) {
     var opts = {
	lines: 13, // The number of lines to draw
	length: 7, // The length of each line
	width: 4, // The line thickness
	radius: 10, // The radius of the inner circle
	corners: 1, // Corner roundness (0..1)
	rotate: 0, // The rotation offset
	color: '#000', // #rgb or #rrggbb
	speed: 1, // Rounds per second
	trail: 60, // Afterglow percentage
	shadow: false, // Whether to render a shadow
	hwaccel: false, // Whether to use hardware acceleration
	className: 'spinner', // The CSS class to assign to the spinner
	zIndex: 2e9, // The z-index (defaults to 2000000000)
	top: 250, // Top position relative to parent in px
	left: 'auto' // Left position relative to parent in px
      };
      var spinner = new Spinner(opts).spin(target);
}


function callIngestionSetInvoice(piSampleString)
{
        var sampleIdentifier = context.getEntityIdentifier()

        var piSampleStringList = piSampleString.split("#")
        var principalInvestigator = piSampleStringList[0]
	var listOfSamples = piSampleStringList.slice(1,piSampleStringList.length)

        dsu.server.useSession(context.getSessionId());
        var parameters = 
        {
                sampleId : sampleIdentifier,
                pI : principalInvestigator,
                listOfSamples : listOfSamples
        };
        
        d3.select("#main").append("div").attr("id", "setInvoice").append("p").text("Setting 'INVOICE SENT' property of " + listOfSamples + "to TRUE");
        var target = document.getElementById('setInvoice');
        spinner(target) 
        dsu.server.createReportFromAggregationService("DSS1", "setInvoiceSent", parameters, hideButtons);		
}


function callIngestionService()
{
        var permIdentifier = context.getEntityIdentifier()
        var mySendEmail = $("#sendEmail").is(':checked')
        var bowtieParameters = $("#Bowtie2Paramter").val()

	d3.select("#main").select("#progress").remove()
	d3.select("#main").select("#vis").remove()
	didCreateVis = false;
	dsu.server.useSession(context.getSessionId());
	createVis()
	var parameters = 
	{
		permId : permIdentifier,
                sendEmail : mySendEmail,
                bowtieParam : bowtieParameters
	};
        
	d3.select("#main").append("div").attr("id", "progress").append("p").text("Starting gc3pie Job...");
        var target = document.getElementById('progress');
        spinner(target) 
	dsu.server.createReportFromAggregationService("DSS1", "triggergc3pie", parameters, displayReturnedTable);
}

 </script>
</head>
<body>
<div id="container" class="container">
<h2>Bowtie 2 Alignment</h2>
<div id="main">



<form role="form">
  <div class="form-group">
    <label for="Bowtie2ParamterLabel">Parameters</label>
    <input type="text" class="form-control" id="Bowtie2Paramter" placeholder="e.g. --very-sensitive --phred33 -q">
  </div>
  <div class="checkbox">
    <label>
      <input type="checkbox" id="sendEmail"> Send Email when job finished
    </label>
  </div>
</form>
<button  class="btn btn-default btn-default"  type="submit" "createInvoice-button" onclick="callIngestionService();">Start Bowtie2 Alignment</button>

    <div class="panel-group" id="accordion">
  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
          Available Bowtie 2 Options
        </a>
      </h4>
    </div>
    <div id="collapseOne" class="panel-collapse collapse">
      <div class="panel-body">
        <pre>
Usage:
  bowtie2 [options]* -x &lt;bt2-idx&gt; {-1 &lt;m1&gt; -2 &lt;m2&gt; | -U &lt;r&gt;} [-S &lt;sam&gt;]

  &lt;bt2-idx&gt;  Index filename prefix (minus trailing .X.bt2).
             NOTE: Bowtie 1 and Bowtie 2 indexes are not compatible.
  &lt;m1&gt;       Files with #1 mates, paired with files in &lt;m2&gt;.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  &lt;m2&gt;       Files with #2 mates, paired with files in &lt;m1&gt;.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  &lt;r&gt;        Files with unpaired reads.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  &lt;sam&gt;      File for SAM output (default: stdout)

  &lt;m1&gt;, &lt;m2&gt;, &lt;r&gt; can be comma-separated lists (no whitespace) and can be
  specified many times.  E.g. '-U file1.fq,file2.fq -U file3.fq'.

Options (defaults in parentheses):

 Input:
  -q                 query input files are FASTQ .fq/.fastq (default)
  --qseq             query input files are in Illumina's qseq format
  -f                 query input files are (multi-)FASTA .fa/.mfa
  -r                 query input files are raw one-sequence-per-line
  -c                 &lt;m1&gt;, &lt;m2&gt;, &lt;r&gt; are sequences themselves, not files
  -s/--skip &lt;int&gt;    skip the first &lt;int&gt; reads/pairs in the input (none)
  -u/--upto &lt;int&gt;    stop after first &lt;int&gt; reads/pairs (no limit)
  -5/--trim5 &lt;int&gt;   trim &lt;int&gt; bases from 5'/left end of reads (0)
  -3/--trim3 &lt;int&gt;   trim &lt;int&gt; bases from 3'/right end of reads (0)
  --phred33          qualities are Phred+33 (default)
  --phred64          qualities are Phred+64
  --int-quals        qualities encoded as space-delimited integers

 Presets:                 Same as:
  For --end-to-end:
   --very-fast            -D 5 -R 1 -N 0 -L 22 -i S,0,2.50
   --fast                 -D 10 -R 2 -N 0 -L 22 -i S,0,2.50
   --sensitive            -D 15 -R 2 -N 0 -L 22 -i S,1,1.15 (default)
   --very-sensitive       -D 20 -R 3 -N 0 -L 20 -i S,1,0.50

  For --local:
   --very-fast-local      -D 5 -R 1 -N 0 -L 25 -i S,1,2.00
   --fast-local           -D 10 -R 2 -N 0 -L 22 -i S,1,1.75
   --sensitive-local      -D 15 -R 2 -N 0 -L 20 -i S,1,0.75 (default)
   --very-sensitive-local -D 20 -R 3 -N 0 -L 20 -i S,1,0.50

 Alignment:
  -N &lt;int&gt;           max # mismatches in seed alignment; can be 0 or 1 (0)
  -L &lt;int&gt;           length of seed substrings; must be &gt;3, &lt;32 (22)
  -i &lt;func&gt;          interval between seed substrings w/r/t read len (S,1,1.15)
  --n-ceil &lt;func&gt;    func for max # non-A/C/G/Ts permitted in aln (L,0,0.15)
  --dpad &lt;int&gt;       include &lt;int&gt; extra ref chars on sides of DP table (15)
  --gbar &lt;int&gt;       disallow gaps within &lt;int&gt; nucs of read extremes (4)
  --ignore-quals     treat all quality values as 30 on Phred scale (off)
  --nofw             do not align forward (original) version of read (off)
  --norc             do not align reverse-complement version of read (off)

  --end-to-end       entire read must align; no clipping (on)
   OR
  --local            local alignment; ends might be soft clipped (off)

 Scoring:
  --ma &lt;int&gt;         match bonus (0 for --end-to-end, 2 for --local)
  --mp &lt;int&gt;         max penalty for mismatch; lower qual = lower penalty (6)
  --np &lt;int&gt;         penalty for non-A/C/G/Ts in read/ref (1)
  --rdg &lt;int&gt;,&lt;int&gt;  read gap open, extend penalties (5,3)
  --rfg &lt;int&gt;,&lt;int&gt;  reference gap open, extend penalties (5,3)
  --score-min &lt;func&gt; min acceptable alignment score w/r/t read length
                     (G,20,8 for local, L,-0.6,-0.6 for end-to-end)

 Reporting:
  (default)          look for multiple alignments, report best, with MAPQ
   OR
  -k &lt;int&gt;           report up to &lt;int&gt; alns per read; MAPQ not meaningful
   OR
  -a/--all           report all alignments; very slow, MAPQ not meaningful

 Effort:
  -D &lt;int&gt;           give up extending after &lt;int&gt; failed extends in a row (15)
  -R &lt;int&gt;           for reads w/ repetitive seeds, try &lt;int&gt; sets of seeds (2)

 Paired-end:
  -I/--minins &lt;int&gt;  minimum fragment length (0)
  -X/--maxins &lt;int&gt;  maximum fragment length (500)
  --fr/--rf/--ff     -1, -2 mates align fw/rev, rev/fw, fw/fw (--fr)
  --no-mixed         suppress unpaired alignments for paired reads
  --no-discordant    suppress discordant alignments for paired reads
  --no-dovetail      not concordant when mates extend past each other
  --no-contain       not concordant when one mate alignment contains other
  --no-overlap       not concordant when mates overlap at all

 Output:
  -t/--time          print wall-clock time taken by search phases
  --un &lt;path&gt;           write unpaired reads that didn't align to &lt;path&gt;
  --al &lt;path&gt;           write unpaired reads that aligned at least once to &lt;path&gt;
  --un-conc &lt;path&gt;      write pairs that didn't align concordantly to &lt;path&gt;
  --al-conc &lt;path&gt;      write pairs that aligned concordantly at least once to &lt;path&gt;
  (Note: for --un, --al, --un-conc, or --al-conc, add '-gz' to the option name, e.g.
  --un-gz &lt;path&gt;, to gzip compress output, or add '-bz2' to bzip2 compress output.)
  --quiet            print nothing to stderr except serious errors
  --met-file &lt;path&gt;  send metrics to file at &lt;path&gt; (off)
  --met-stderr       send metrics to stderr (off)
  --met &lt;int&gt;        report internal counters & metrics every &lt;int&gt; secs (1)
  --no-head          supppress header lines, i.e. lines starting with @
  --no-sq            supppress @SQ header lines
  --rg-id &lt;text&gt;     set read group id, reflected in @RG line and RG:Z: opt field
  --rg &lt;text&gt;        add &lt;text&gt; ("lab:value") to @RG line of SAM header.
                     Note: @RG line only printed when --rg-id is set.
  --omit-sec-seq     put '*' in SEQ and QUAL fields for secondary alignments.

 Performance:
  -o/--offrate &lt;int&gt; override offrate of index; must be &gt;= index's offrate
  -p/--threads &lt;int&gt; number of alignment threads to launch (1)
  --reorder          force SAM output order to match order of input reads
  --mm               use memory-mapped I/O for index; many 'bowtie's can share

 Other:
  --qc-filter        filter out reads that are bad according to QSEQ filter
  --seed &lt;int&gt;       seed for random number generator (0)
  --non-deterministic seed rand. gen. arbitrarily instead of using read attributes
  --version          print version information and quit
  -h/--help          print this usage message
</pre>
</div>
</div>
</div>
</div>

</div>
</div>
</body>
</html>
