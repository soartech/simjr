package com.example.simjr.hello;

import com.soartech.simjr.scripting.ResourceScriptProvider;

public class HelloWorldScriptProvider extends ResourceScriptProvider {
	{
		add("hello", "hello.js");
		add("example", "com/example/hello/common.js");
	}
}
