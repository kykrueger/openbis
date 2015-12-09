/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/Enum" ], function(stjs, Enum) {
	var ProjectRelationType = function() {
		Enum.call(this, [ "SPACE", "EXPERIMENT" ]);
	};
	stjs.extend(ProjectRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ProjectRelationType();
})
