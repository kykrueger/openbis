define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var Right = function() {
		Enum.call(this, [ "UPDATE", "DELETE", "CREATE", "FREEZE",
			"CREATE_PROJECT", "CREATE_EXPERIMENT", "CREATE_SAMPLE",
			"CREATE_CHILD", "CREATE_PARENT", "CREATE_COMPONENT", "CREATE_CONTAINER" ]);
	};
	stjs.extend(Right, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new Right();
})