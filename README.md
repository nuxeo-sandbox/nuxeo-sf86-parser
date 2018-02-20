# About

"SF86" refers to "Standard Form 86", which is a questionnaire used for application to National Security Positions. See [https://en.wikipedia.org/wiki/Standard_Form_86](https://en.wikipedia.org/wiki/Standard_Form_86)

This form is mapped to an XML format as well. This plug-in leverages Nuxeo's XML Importer Service (from the [importer framework](https://doc.nuxeo.com/nxdoc/choosing-how-to-import-data-in-the-nuxeo-platform/#nuxeo-platform-importer)) to ingest and extract data from SF86 XML files using standard Nuxeo [Binary Metadata](https://doc.nuxeo.com/nxdoc/binary-metadata/) mapping.

Note that there is currently a dependency on a specific Studio project for this plug-in.

# Requirements

Build requires the following software:
- git
- maven

# Build

```
mvn clean install
# or 
mvn clean install -DskipTests
```

# Deploy

Install the marketplace package on your Nuxeo instance.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).
