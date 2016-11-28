package com.soartech.simjr.example;

import com.soartech.simjr.scripting.ResourceScriptProvider;

public class ExampleScriptProvider extends ResourceScriptProvider {
	{
		add("hello", "hello.js");
		add("example", "com/soartech/simjr/example/common.js");
	}
}
