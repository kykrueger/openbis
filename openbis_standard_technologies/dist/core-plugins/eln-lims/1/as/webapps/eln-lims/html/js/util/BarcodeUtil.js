var BarcodeUtil = new function() {

    var barcodeTimeout = false;
    var barcodeReader = "";

    var readSample = function() {
        // Trigger search if needed
        // permID Format 23 char, 1 hyphen: 20170912112249208-38888
        // UUID Format 36 char, 4 hyphens: 123e4567-e89b-12d3-a456-426655440000
        var rules = {};
        if(barcodeReader.length === 36) {
            rules["UUIDv4"] = { type: "Property/Attribute", 	name: "PROP.$BARCODE", operator : "thatEqualsString", value: barcodeReader };
        } else if(barcodeReader.length === 23) {
            rules["UUIDv4"] = { type: "Property/Attribute", 	name: "ATTR.PERM_ID", operator : "thatEqualsString", value: barcodeReader };
        }

        if(rules) {
            var criteria = {};
            criteria.entityKind = "SAMPLE";
            criteria.logicalOperator = "AND";
            criteria.rules = rules;

            mainController.serverFacade.searchForSamplesAdvanced(criteria, { only : true, withProperties: true },
            function(results) {
                if(results.totalCount === 1) {
                    mainController.changeView('showViewSamplePageFromPermId', results.objects[0].permId.permId);
                }
            });
        }
    }

    this.enableAutomaticBarcodeReading = function() {
        document.addEventListener('keyup', function(event) {
            if(!barcodeTimeout) {
                barcodeTimeout = true;
                var timeoutFunc = function() {
                    readSample();
                    // reset
                    barcodeTimeout = false;
                    barcodeReader = "";
                }
                setTimeout(timeoutFunc, 1000);
            }
            barcodeReader += event.key;
        });
    }

    this.preGenerateBarcodes = function(views) {
        views.header.append($("<h2>").append("Barcode Generator"));
	    var $generateBtn = FormUtil.getButtonWithText("Generate Barcodes", function() {}, "btn-primary");
        $generateBtn.css("margin-bottom", "14px");

        var $toolbar = $("<span>");

        var $numberDropdown = FormUtil.getDropdown([
            { label: '10', value: 10, selected: true },
            { label: '25', value: 25 },
            { label: '50', value: 50 },
            { label: '100', value: 100 }
        ]);
        $toolbar.append($generateBtn).append($("<span>", { style:"width:50%; margin-left: 10px; display:inline-block;"}).append($numberDropdown));
        views.header.append($toolbar);

        var _this = this;
        $generateBtn.click(function() {
            views.content.empty();
            var value = parseInt($numberDropdown.val());
            for(var idx = 0; idx < value; idx++) {
                _this.addBarcode(views.content, idx);
            }
        });

        this.preloadLibrary();
    }

    this.preloadLibrary = function() {
        var $hiddenContainer = $("<div>", { style : "display:none;" });
        $(document.body).append($hiddenContainer);
        $hiddenContainer.append($('<center>').append($('<canvas>', { id : "barcode-canvas-preload", width : 1, height : 1, style : "border:1px solid #fff;visibility:hidden" })));
        this.generateBarcode("barcode-canvas-preload", "code128", "Barcode", "Text", function() {
            $hiddenContainer.remove();
        });
    }

    this.addBarcode = function(content, idx) {
        var uuid = Util.guid();
        content.append($('<br>'));
        content.append($('<center>').append($('<canvas>', { id : "barcode-canvas-" + idx, width : 1, height : 1, style : "border:1px solid #fff;visibility:hidden" })));
        this.generateBarcode("barcode-canvas-" + idx, "code128", uuid, uuid);
    }

    this.readBarcode = function(entity) {
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Save Barcode' });
        $btnAccept.prop("disabled",true);


        var $barcodeReader = $('<input>', { 'type': 'text', 'placeholder': 'barcode' });
        $barcodeReader.keyup(function() {
            var UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
            var isValid = UUID_REGEX.exec($barcodeReader.val());
            if(isValid) {
                $btnAccept.prop("disabled", false);
            } else {
                $btnAccept.prop("disabled", true);
            }
        });

        $btnAccept.click(function(event) {
            Util.blockUINoMessage();
            require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SamplePermId" ],
            function(SampleUpdate, SamplePermId) {
                var sample = new SampleUpdate();
                sample.setSampleId(new SamplePermId(entity.permId));
                sample.setProperty("$BARCODE", $barcodeReader.val());
                mainController.openbisV3.updateSamples([ sample ]).done(function(result) {
                    Util.unblockUI();
                    Util.showInfo("Barcode Updated", function() {
                        mainController.changeView('showViewSamplePageFromPermId', entity.permId);
                    }, true);
                }).fail(function(result) {
                    Util.showFailedServerCallError(result);
                });
            });
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
        $btnCancel.click(function(event) {
            Util.unblockUI();
        });

        $window.append($('<legend>').append("Update Barcode"));
        $window.append($('<br>'));
        $window.append($('<center>').append($barcodeReader));
        $window.append($('<br>'));
        $window.append($btnAccept).append('&nbsp;').append($btnCancel);

        var css = {
            'text-align' : 'left',
            'top' : '15%',
            'width' : '70%',
            'height' : '400px',
            'left' : '15%',
            'right' : '20%',
            'overflow' : 'auto'
        };

        Util.blockUI($window, css);
    }

    this.showBarcode = function(entity) {
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Download' });
        $btnAccept.click(function(event) {
            var canvas = document.getElementById('barcode-canvas');
            canvas.toBlob(function (blob) {
                saveAs(blob, entity.permId + '.png');
            }, 'image/png', 1);
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
		$btnCancel.click(function(event) {
			Util.unblockUI();
		});

		$window.append($('<legend>').append("Print Barcode"));
	    $window.append($('<br>'));
	    $window.append($('<center>').append($('<canvas>', { id : "barcode-canvas", width : 1, height : 1, style : "border:1px solid #fff;visibility:hidden" })));
	    $window.append($('<br>'));
	    $window.append($btnAccept).append('&nbsp;').append($btnCancel);

        var css = {
            'text-align' : 'left',
            'top' : '15%',
            'width' : '70%',
            'height' : '400px',
            'left' : '15%',
            'right' : '20%',
            'overflow' : 'auto'
        };

        Util.blockUI($window, css);

        var barcode = null;
        if(entity.properties && entity.properties["$BARCODE"]) {
            barcode = entity.properties["$BARCODE"];
        } else {
            barcode = entity.permId;
        }

        this.generateBarcode("barcode-canvas", "code128", barcode, barcode);
    }

    this.supportedBarcodes = function() {
        return [{
                    id : "code128",
                    value : "Code 128"
                },{
                    id : "qrcode",
                    value : "QR Code"
                },{
                    id : "microqrcode",
                    value : "Micro QR Code"
            }];
    }

    this.generateBarcode = function(canvasId, barcodeType, text, altx, action) {
        var elt  = symdesc[barcodeType];
        var opts = {};
        var rot  = "N";
        var monochrome = true;
        var scaleX = 2;
        var scaleY = 2;

        var bw = new BWIPJS(bwipjs_fonts, monochrome);

        var canvas = document.getElementById(canvasId);
        canvas.height = 1;
        canvas.width  = 1;
        canvas.style.visibility = 'hidden';

        // Add the alternate text
        if (altx) {
            opts.alttext = altx;
            opts.includetext = true;
        }
        // We use mm rather than inches for height - except pharmacode2 height
        // which is expected to be in mm
        if (+opts.height && elt.sym != 'pharmacode2') {
            opts.height = opts.height / 25.4 || 0.5;
        }
        // Likewise, width.
        if (+opts.width) {
            opts.width = opts.width / 25.4 || 0;
        }
        // BWIPP does not extend the background color into the
        // human readable text.  Fix that in the bitmap interface.
        if (opts.backgroundcolor) {
            bw.bitmap(new Bitmap(canvas, rot, opts.backgroundcolor));
            delete opts.backgroundcolor;
        } else {
            bw.bitmap(new Bitmap(canvas, rot));
        }

        // Set the scaling factors
        bw.scale(scaleX, scaleY);

        // Add optional padding to the image
        bw.bitmap().pad(+opts.paddingwidth*scaleX || 0,
                        +opts.paddingheight*scaleY || 0);

        try {
            // Call into the BWIPP cross-compiled code.
            BWIPP()(bw, elt.sym, text, opts);

            // Allow the font manager to demand-load any required fonts
            // before calling render().
            bwipjs_fonts.loadfonts(function(e) {
                if (e) {
                    Util.manageError(e);
                } else {
                    // Draw the barcode to the canvas
                    bw.render();
                    canvas.style.visibility = 'visible';
                    if(action) {
                        action();
                    }
                }
            });
        } catch (e) {
            // Watch for BWIPP generated raiseerror's.
            var msg = ''+e;
            if (msg.indexOf("bwipp.") >= 0) {
                Util.manageError(msg);
            } else if (e.stack) {
                Util.manageError(e.stack);
            } else {
                Util.manageError(e);
            }
        }
    }
}