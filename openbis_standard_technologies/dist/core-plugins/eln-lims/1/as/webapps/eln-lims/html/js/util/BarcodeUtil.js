var BarcodeUtil = new function() {

    this.showBarcode = function(entity) {
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Cancel' });
		$btnCancel.click(function(event) {
			Util.unblockUI();
		});

		$window.append($('<legend>').append("Barcode"));
	    $window.append($('<br>'));
	    $window.append($('<canvas>', { id : "barcode-canvas", width : 1, height : 1, style : "border:1px solid #fff;visibility:hidden" }));
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
        this.generateBarcode("barcode-canvas", "code128", entity.permId, entity.permId);
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

    this.generateBarcode = function(canvasId, barcodeType, text, altx) {
        var elt  = symdesc[barcodeType];
        var opts = "includetext";
        var rot  = "N";
        var monochrome = true;
        var scaleX = 2;
        var scaleY = 2;

        var bw = new BWIPJS(bwipjs_fonts, monochrome);

        var canvas = document.getElementById(canvasId);
        canvas.height = 1;
        canvas.width  = 1;
        canvas.style.visibility = 'hidden';

        // Convert the options to a dictionary object, so we can pass alttext with
        // spaces.
        var tmp = opts.split(' ');
        opts = {};
        for (var i = 0; i < tmp.length; i++) {
            if (!tmp[i]) {
                continue;
            }
            var eq = tmp[i].indexOf('=');
            if (eq == -1) {
                opts[tmp[i]] = true;
            } else {
                opts[tmp[i].substr(0, eq)] = tmp[i].substr(eq+1);
            }
        }

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

        var ts0 = Date.now();
        try {
            // Call into the BWIPP cross-compiled code.
            BWIPP()(bw, elt.sym, text, opts);

            // Allow the font manager to demand-load any required fonts
            // before calling render().
            var ts1 = Date.now();
            bwipjs_fonts.loadfonts(function(e) {
                if (e) {
                    Util.manageError(e);
                } else {
                    // Draw the barcode to the canvas
                    bw.render();
                    canvas.style.visibility = 'visible';
                }
            });
        } catch (e) {
            // Watch for BWIPP generated raiseerror's.
            var msg = ''+e;
            if (msg.indexOf("bwipp.") >= 0) {
                Util.manageError(msg);
            } else if (e.stack) {
                Util.manageError(stack);
            } else {
                Util.manageError(e);
            }
            return;
        }
    }
}