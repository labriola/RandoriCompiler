package demo.inject
{
import demo.foo.ClassA;
import demo.foo.ClassB;

import randori.jquery.JQuery;

public class InjectTest
{
    [Inject]
    public var injectField:ClassA;
    
    [Inject]
    public function get injectAccessor():String
    {
        return null;
    }
    
    public function set injectAccessor(value:String):void
    {
    }
    
    [View]
    public var viewField:JQuery;
    
    public function InjectTest(param1:String, param2:int = 1)
    {
    }
    
    [Inject]
    public function injectMethod(param1:ClassB, param2:String = "foo"):void
    {
    }
}
}