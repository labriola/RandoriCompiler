package demo.foo
{

import randori.jquery.Event;
import randori.jquery.JQuery;
import randori.jquery.JQueryStatic;
import randori.webkit.fileapi.FileReader;
import randori.webkit.page.Window;

public class ClassB extends ClassA
{
    private var thenContracts:Array;
    
    public var field1:String = "Hello";
    
    public var field2:int = 42;
    
    public static var onSelectHandler:Function;
    
    public function get type():* {
        return null;
    }
    
    private var j:JQuery;
    
    public static var FOO:Object;
    
    public function get accessor1():int{}
    
    public function set accessor1(value:int):void{}
    
    public function ClassB(param1:Object, param2:int = 42, param3:String = 'foo')
    {
        super(param1);
        field2 = param2;
    }
    
    protected function method1(param:String):String
    {
        c.foo.bar(42);
        var a:int;
        var c:ClassA;
        var b;
        c.data = "foo";
        c.data = function(foo, bar) {
            function () {
                Window.alert("dd");
            }
        };
        // Not Impl
        // super.data = "";
        data = a = data;
        data = data[a[data[field1][0]]] = c.foo.concat(field1)[0];
        c.foo.bar(42);
        data = data[a][b][a = data];
        return data;
    }
    
    public function while_binary_getter()
    {
        while (thenContracts.length > 0) {
            
        }
    }
    
    public function this_no_parent()
    {
        var foo:ClassB = this;
    }
    
    public function for_each_statement()
    {
        for each (var id:String in thenContracts) 
        {
        }
    }
    
    public function for_with_vector_length()
    {
        var viewPoints:Vector.<ClassA> = new Vector.<ClassA>();
        for (var i:int = 0; i < viewPoints.length; i++) {
        }
    }
    
    public function as_cast()
    {
        var a:Object;
        var b:Array = a as Array;
    }
    
    public function is_type_check()
    {
        if (field1 is Array) 
        {
        }
    }
    
    public function undefined_literal()
    {
        if (field1 != undefined)
        {
        }
    }
    
    public function static_var_qualified()
    {
        FOO[foo] = "bar";
    }
    
    public function static_var_qualified_prepend_Type()
    {
        ClassB.FOO[foo] = "bar";
    }
    
    [JavaScriptProperty(name="FileReader")]
    /**
     *  @see randori.webkit.fileapi.FileReader
     */
    public static function get FileReader_():FileReader { return null; }
    public static function set FileReader_(value:FileReader):void { }
    
    public function FileReader_use_get_set_rename():void
    {
        FileReader_ = new FileReader();
        var a:FileReader = ClassB.FileReader_;
    }
    
    public function method_call_rename():void
    {
        j.show();
    }
    
    [JavaScriptMethod(name="fooBar")]
    public function method_annotation_rename()
    {
    }
    
    public function window_static_method():void
    {
        Window.alert("foo");
    }
    
    public function window_static_accessor():void
    {
        if (Window.console != null)
            Window.console.log("foo");
    }
    
    public function member_accessor_get_and_set():void
    {
        var c:ClassA;
        c.foo.bar(42);
    }
    
    public function default_parameters(foo:int, bar:String = "42", baz:Number = 0.42):void
    {
        var a:Object = baz;        
    }
    
    public function window_reduction():void
    {
        var nextLevel:* = Window.window;
    }
    
    public function this_get_accessor_explicit():void
    {
        var c:Object;
        c = this.accessor1;
    }
    
    public function this_get_accessor_explicit():void
    {
        var c:Object;
        c = this.accessor1;
    }
    
    public function this_get_accessor_anytype_access():void
    {
        if (type.injectionPoints == null) {
            
        }
    }
    
    public function new_anytype():void
    {
        var instance:Object = null;
        
        if (false)
            instance = new type();
    }
    
    public function JQueryStatic_J():void
    {
        foo.injectPotentialNode(FOO, JQueryStatic.J(field1));
    }
    
    public function anonymous_function_argument_delegate():void
    {
        Window.setTimeout(function():void {
            method1(foo);
        }, 1);
    }
    
    public function anonymous_function_field_delegate():void
    {
        onSelectHandler = function(event:Event) {
            foo = "Hello";
        }
    }
    
    [JavaScriptCode(file="resolve.js")]
    public function jscontext():void
    {
    }
    
    public function conditional_lhs_getter():void
    {
        if ((data == null) || (data.length == 0)) {
            return;
        }
    }
    
    public function functioncall_getter_invoke():void
    {
        var a;
        a = renderFunction(j, data[j]);
    }
    
    public function const_string():void
    {
        const RANDORI_VENDOR_ITEM_EXPRESSION:String = "\\s?-randori-([\\w\\W]+?)\\s?:\\s?[\"\']?([\\w\\W]+?)[\"\']?;";
        var anyVendorItems:RegExp = new RegExp(RANDORI_VENDOR_ITEM_EXPRESSION, "g");
    }
    
    
}
}