/**
 *  @author pkupczyk
 */
define(["stjs", "dto/id/ObjectTechId", "dto/id/deletion/IDeletionId"], function (stjs, ObjectTechId, IDeletionId) {
    var DeletionTechId = function(techId) {
        ObjectTechId.call(this, techId);
    };
    stjs.extend(DeletionTechId, ObjectTechId, [ObjectTechId, IDeletionId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.deletion.DeletionTechId';
        constructor.serialVersionUID = 1;
    }, {});
    return DeletionTechId;
})