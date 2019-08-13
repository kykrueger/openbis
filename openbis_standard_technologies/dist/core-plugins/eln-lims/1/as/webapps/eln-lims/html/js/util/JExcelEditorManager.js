
var JExcelEditorManager = new function() {
    this.jExcelEditors = {}

    this.getOnChange = function(guid, propertyCode, entity) {
        var _this = this;
        return function() {
            var jExcelEditor = _this.jExcelEditors[guid];
            if(jExcelEditor) {
                var data = jExcelEditor.getData();
                var style = jExcelEditor.getStyle();
                var meta = jExcelEditor.getMeta();
                var jExcelEditorValue = {
                    data : data,
                    style : style,
                    meta : meta
                }
                entity.properties[propertyCode] = "<DATA>" + JSON.stringify(jExcelEditorValue) + "</DATA>";
            }
        }
    }

    this.getObjectFunction = function(guid) {
        var _this = this;
        return function() {
            var component = "<div>"
                component += "<legend>Insert Object</legend>";
            	component += "<div>";

                component += "<div class='form-group'>";
                component += "<label class='control-label'>Object:</label>";
                component += "<div>";
                component += "<div id='objectSelector'></div>";
                component += "</div>";
                component += "</div>";

                component += "<div class='form-group'>";
                component += "<label class='control-label'>Options:</label>";
                component += "<div class='controls'>";
                component += "<span class='checkbox'><label><input type='checkbox' id='insertHeaders'> Insert Headers </label></span>";
                component += "</div>";
                component += "</div>";

                component += "</div>";
                component += "</div>";
                Util.blockUI(component + "<a class='btn btn-default' id='insertAccept'>Accept</a> <a class='btn btn-default' id='insertCancel'>Cancel</a>", FormUtil.getDialogCss());

                var advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(true, true, "Select Object", false, true, false, false);
                advancedEntitySearchDropdown.init($("#objectSelector"));

                $("#insertCancel").on("click", function(event) {
                    Util.unblockUI();
                });

                $("#insertAccept").on("click", function(event) {
                    var jExcelEditor = _this.jExcelEditors[guid];
                    var insertHeaders = $("#insertHeaders")[0].checked;
                    var selected = advancedEntitySearchDropdown.getSelected();
                    if(selected.length > 0) {

                    }
                    Util.unblockUI();
                });
        }
    }

	this.createField = function($container, mode, propertyCode, entity) {
	    $container.attr('style', 'width: 100%; height: 450px; overflow-y: scroll; overflow-x: scroll;');

	    var data = [];
	    var style = null;
	    var meta = null;

	    if(entity.properties && entity.properties[propertyCode]) {
	        var jExcelEditorValueAsStringWithTags = entity.properties[propertyCode];
	        var jExcelEditorValue = null;
	        if(jExcelEditorValueAsStringWithTags) {
	            var jExcelEditorValueAsStringNoTags = jExcelEditorValueAsStringWithTags.substring(6, jExcelEditorValueAsStringWithTags.length - 7);
                jExcelEditorValue = JSON.parse(jExcelEditorValueAsStringNoTags);
	        }
	        if(jExcelEditorValue) {
	            data = jExcelEditorValue.data;
	            style = jExcelEditorValue.style;
	            meta = jExcelEditorValue.meta;
	        }
	    }

        var guid = Util.guid();

        var options = {
                    data: data,
                    style: style,
                    meta: meta,
                    editable : mode !== FormMode.VIEW,
                    minDimensions:[30, 30],
                    toolbar: null,
                    onchange: null,
                    onchangestyle: null,
                    onchangemeta: null
        };

        if(mode === FormMode.VIEW) {
            options.allowInsertRow = false;
            options.allowManualInsertRow = false;
            options.allowInsertColumn = false;
            options.allowManualInsertColumn = false;
            options.allowDeleteRow = false;
            options.allowDeleteColumn = false;
            options.allowRenameColumn = false;
            options.allowComments = false;

            options.contextMenu = function(obj, x, y, e) {
                return [];
            }
        } else {
            var onChangeHandler = this.getOnChange(guid, propertyCode, entity);
            options.onchange = onChangeHandler;
            options.onchangestyle = onChangeHandler;
            options.onchangemeta = onChangeHandler;

            options.toolbar = [
                    { type:'select', k:'font-family', v:['Arial','Verdana'] },
                    { type:'select', k:'font-size', v:['9px','10px','11px','12px','13px','14px','15px','16px','17px','18px','19px','20px'] },
                    { type:'i', content:'format_align_left', k:'text-align', v:'left' },
                    { type:'i', content:'format_align_center', k:'text-align', v:'center' },
                    { type:'i', content:'format_align_right', k:'text-align', v:'right' },
                    { type:'i', content:'format_bold', k:'font-weight', v:'bold' },
                    { type:'color', content:'format_color_text', k:'color' },
                    { type:'color', content:'format_color_fill', k:'background-color' },
                    { type:'i', content:'input', onclick: this.getObjectFunction(guid) },
            ];
        }

        var jexcelField = jexcel($container[0], options);
        this.jExcelEditors[guid] = jexcelField;
	}
}