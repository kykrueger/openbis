
var JExcelEditorManager = new function() {
    this.jExcelEditors = {}

    this.getOnChange = function(guid, propertyCode, entity) {
        var _this = this;
        return function() {
            var jExcelEditor = _this.jExcelEditors[guid];
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
	    var onChangeHandler = this.getOnChange(guid, propertyCode, entity);

        var toolbar = null;

        if(mode == FormMode.EDIT) {
            toolbar = [
                    { type:'select', k:'font-family', v:['Arial','Verdana'] },
                    { type:'select', k:'font-size', v:['9px','10px','11px','12px','13px','14px','15px','16px','17px','18px','19px','20px'] },
                    { type:'i', content:'format_align_left', k:'text-align', v:'left' },
                    { type:'i', content:'format_align_center', k:'text-align', v:'center' },
                    { type:'i', content:'format_align_right', k:'text-align', v:'right' },
                    { type:'i', content:'format_bold', k:'font-weight', v:'bold' },
                    { type:'color', content:'format_color_text', k:'color' },
                    { type:'color', content:'format_color_fill', k:'background-color' },
            ];
        }
        var jexcelField = jexcel($container[0], {
            data: data,
            style: style,
            meta: meta,
            editable : mode == FormMode.EDIT,
            minDimensions:[30, 30],
            toolbar: toolbar,
            onchange: onChangeHandler
        });

        this.jExcelEditors[guid] = jexcelField;
	}
}