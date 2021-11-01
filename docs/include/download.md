{% macro downloads(version) -%}
## Downloads

### Eclipse Language Workbench Environment

With embedded JVM:

* {{ release[version].eclipse_lwb.install.jvm.link.macos }}
* {{ release[version].eclipse_lwb.install.jvm.link.linux }}
* {{ release[version].eclipse_lwb.install.jvm.link.windows }}

Without embedded JVM:

* {{ release[version].eclipse_lwb.install.link.macos }}
* {{ release[version].eclipse_lwb.install.link.linux }}
* {{ release[version].eclipse_lwb.install.link.windows }}

Repository for installing into an existing Eclipse installation:  `{{ release[version].eclipse_lwb.repository }}`
{%- endmacro %}
