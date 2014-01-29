==================
Template Rendering
==================

Megapode uses Velocity for it's template rendering. Each actor has it's own rendering engine,
and they operate in isolation. 

Templates are placed in the classpath of the application. 

For most templates, the DefaultRenderer should suffice. But if not, you can define 
your own renderer
Renderers are once again, annotated with com.gravspace.annotations.Renderer, and implement
com.gravspace.abstractions.IRenderer or extend com.gravspace.bases.RendererBase. 

Unlike the Task or Calculations, the renderes only have one Interface, IRenderer, 
with the methods:: 

    // if we don't have a defined Template Name
    Future<String> render(String template, Map<String, ?> context);
    
    //if we already have a defined Template Name
    Future<String> render(Map<String, ?> context);

Therefore from a ConcurrentCallable the proxy has two get methods::
    
    IRenderer renderer = Renderers.getDefault(this);
    // or
    IRenderer renderer = Renderers.getDefault(RendererClass.class, this); 

The default renderer is therefore used as such ::

    IRenderer renderer = Renderers.getDefault(this);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("obj", "obj");
    Future<String> rendered_template = renderer.render("template.your.hompage.html", context); 
