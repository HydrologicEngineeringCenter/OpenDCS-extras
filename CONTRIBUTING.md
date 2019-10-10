# Contributing to Our Projects, Version 1.5

**NOTE: This CONTRIBUTING.md is for software contributions. You do not need to follow the Developer's Certificate of Origin (DCO) process for commenting on the Code.mil repository documentation, such as CONTRIBUTING.md, INTENT.md, etc. or for submitting issues.**

Thanks for thinking about using or contributing to this software ("Project") and its documentation!

* [Policy & Legal Info](#policy)
* [Getting Started](#getting-started)
* [Submitting an Issue](#submitting-an-issue)
* [Submitting Code](#submitting-code)

## Policy

### 1. Introduction

The project maintainer for this Project will only accept contributions using the Developer's Certificate of Origin 1.1 located at [developercertificate.org](https://developercertificate.org) ("DCO"). The DCO is a legally binding statement asserting that you are the creator of your contribution, or that you otherwise have the authority to distribute the contribution, and that you are intentionally making the contribution available under the license associated with the Project ("License").

### 2. Developer Certificate of Origin Process

Before submitting contributing code to this repository for the first time, you'll need to sign a Developer Certificate of Origin (DCO) (see below). To agree to the DCO, add your name and email address to the [CONTRIBUTORS.md](https://github.com/Code-dot-mil/code.mil/blob/master/CONTRIBUTORS.md) file. At a high level, adding your information to this file tells us that you have the right to submit the work you're contributing and indicates that you consent to our treating the contribution in a way consistent with the license associated with this software (as described in [LICENSE.md](https://github.com/Code-dot-mil/code.mil/blob/master/LICENSE.md)) and its documentation ("Project").

### 3. Important Points

Pseudonymous or anonymous contributions are permissible, but you must be reachable at the email address provided in the Signed-off-by line.

If your contribution is significant, you are also welcome to add your name and copyright date to the source file header.

U.S. Federal law prevents the government from accepting gratuitous services unless certain conditions are met. By submitting a pull request, you acknowledge that your services are offered without expectation of payment and that you expressly waive any future pay claims against the U.S. Federal government related to your contribution.

If you are a U.S. Federal government employee and use a `*.mil` or `*.gov` email address, we interpret your Signed-off-by to mean that the contribution was created in whole or in part by you and that your contribution is not subject to copyright protections.

### 4. DCO Text

The full text of the DCO is included below and is available online at [developercertificate.org](https://developercertificate.org):

```txt
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
1 Letterman Drive
Suite D4700
San Francisco, CA, 94129

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.

Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

## Getting Started

The code in this repository is to extend the features available in the OpenDCS utility (from http://covesw.net/). To build this library you will need a least a copy of the main jar that is provided in that software.
Unfortunately at this time there are some issues with distributing OpenDCS source code. While anyone is welcome to contribute, the intended audience must being the agencies that currently use the tool.

The maven build system is used. Many of the dependencies will be found automatically, for the rest you will need to download them manually and use the mvn install:install-file to identify it as shown.

**NOTE: If this is too complicated and you have verified that the code compiles with algoedit, please indicate so in your pull request. I understand that this will be very new to most contributing code but this a standard way to handle things and over time this will get easier to deal with as we adapt.

**NOTE2: Even if you do not use maven, when submitting the request please structure the code as shown java code goes in src/main/java/ and then the directory structure follows your pacakage name. 

The the code is organized into several branches. The master branch will always be for building against the newest version of OpenDCS. There will also be a branch for each named release.



### Making Changes


Now you're ready to [clone the repository](https://help.github.com/articles/cloning-a-repository/) are start looking at things. If you are going to submit any changes you make please read the Submitting changes section below.


### Code Style

If you are editing an existing file please be consistent with the style in the file. For OpenDCS algorithms, please copy an existing algorithm so that the commented section remain and certain automated tools can still be used with the files.

Otherwise normal Java formatting should be used.

## Submitting an Issue

You should feel free to [submit an issue](https://github.com/<needs name>) on our GitHub repository for anything you find that needs attention on the website. That includes content, functionality, design, or anything else!

### Submitting a Bug Report

When submitting a bug report, please be sure to include accurate and thorough information about the problem you're observing. Be sure to include:

* Steps to reproduce the problem,
* What you expected to happen,
* What actually happened (or didn't happen), and
* Technical details including the specific version number of OpenDCS you are using
* Sanitized logs, if possible.

## Submitting Code

Please [fork](https://help.github.com/en/articles/fork-a-repo) the repository on github and create a [branch in Git](https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging) if you are making changes to existing code.

The recommended branch naming is the version followed by a simple name. E.G. 6.6RC03_newinflow.

If you are submitting an entire new OpenDCS algorithms working on the main branch is fine, though a branch is preferred. 

Once you have made your changes submit a [pull request](https://help.github.com/en/articles/creating-a-pull-request-from-a-fork).

Barring nothing working at all or the code not being related to OpenDCS your contributions will be accepted.

### Check Your Changes

Before submitting your pull request, you should run the build process locally first to ensure things are working as expected.

The project is setup as a maven project and using either the command line or any IDE should work to build the project.

```sh
mvn compile
```

There is a method of unit test avaiable for testing algorithms, it is not complete, if possible please create tests and run them to verify operation.

If you are unsure how to make the test but have enough information (input time series, correct output time series, at various parameters) submit an issue with that information and we will help you set it up.



