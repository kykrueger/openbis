/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectDeletionOptions) {
    var SampleDeletionOptions = function() {
        AbstractObjectDeletionOptions.call(this);
    };
    stjs.extend(SampleDeletionOptions, AbstractObjectDeletionOptions, [AbstractObjectDeletionOptions], function(constructor, prototype) {
        prototype['@type'] = 'SampleDeletionOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return SampleDeletionOptions;
})