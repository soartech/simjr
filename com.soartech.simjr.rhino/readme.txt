This is a plugin wrapper around the Rhino JavaScript engine. The sole purpose
of the plugin is to include the "DynamicImport-Package: *" property in 
MANIFEST.MF. This allows JavaScript code to find code in any loaded plugins
without having to explicitly state which packages to import ahead of time.
It's a bit of a hack, but the easiest way to support Sim Jr's use of
JavaScript for scenario configuration.