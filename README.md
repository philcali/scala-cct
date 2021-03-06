# Scala Course Converter Tool

This conversion tool is a complete ground up rewrite from LSU's Java
[cct](https://tigerbytes2.lsu.edu/users/pcali1/moodlecct/index.html) (cover your eyes!). The software
desperately needed a face-lift, so I gave it one. The conversion process is entirely handled through
Scala (the xml parsing and what not). I found Scala to be a perfect fit for this type of problem.


The tool is now complete with a test suite and an extension API (which I will go into later).

*Note*: Development on this project has pretty much stopped for now (though I've taken the opportunity to upgrade
build software). People can do whatever they want with it. 

## The Ideas

Some ideas if I ever get back into the swing of things:

 * Split up the project into library and conscript projects
 * Add more conversion pieces
 * Provide a better extension API (I had some ideas on this)
 * Make the default conversion page prettier ... It's pretty horrible as is.

---

## How it works now

One app, two functions. You can now convert courses on the command-line, convert entire directories
of courses, or launch a web site and takes in an archive and spits out a converted course.


I recommend looking at the for the converter to see how it works.

---

## The Conversion

The conversion process is defined by command-line options through
--knowledge and --transformer

---

## Knowledge

A knowledge is simply the bit of Intelligence the converter uses to
"understand" how to interpret its input. This converter has two
knowledges built into it: A blackboard 6.5 < 8.0 archive understanding,
and some internal archive called a course dump.


Any programmer can easily extend the converter's functionality by
implementing a `Knowledge`. The converter scans the classpath for known
Knowledges, and gives the user the ability to use it.


A knowledge takes a given archive, or whatever (it could be anything
really, a database, a document, a tarball, a webpage), turn that into
sets of objects for conversion. This object model is important to the
transformer.

---

## Transformer

Not the robot in disguise, though that is a perfectly valid definition.

A transformer takes an object model, and transforms it into something
real, (a database, a document, an archive, a web page, etc). This
converter comes with two: A moodle 1.8 < 2.0 archive, and a course dump.

Like a knowledge, a programmer can extend the converter's functionality by
simply implementing a `Transformer`.

---

## How it works (Continued)

**Note**: the converter comes with a knowledge and transformer of its
own format called `dumps`. This is important for users who don't have a
corresponding transformer for their system, or just want the data from
a knowledge.

---

## Test Suite

The test suite for the Converter is very expansive and also extensible.
Any programmer writing a `Knowledge` or a `Transformer` can extend a
knowledge or transformer test cases, respectively.

It is my recommendation to any developer working on a new knowledge or
transformer, to first start with a test case. Once a test case is fully
implemented, it will work with the main converter.

---

## Options

    cct [--web] [--port=80] [--knowledge=blackboard[:package]] [-r] [--input=path] 
        [--transformer=moodle[:package]] [--output=directory]
    
    Options:
    -h, --help         | prints out this help
    -w, --web          | starts a web interface
    -p, --port         | runs web server on specified port: default 80
    -k, --knowledge    | uses knowledge from KnowledgeTag
    -t, --transformer  | uses transformer from TransformerTag
    -r, --recursive    | uses files in recursive as input (ignored in web mode)
    -i, --input        | uses this input path (ignored in web mode)
    -o, --output       | dumps converted to this directory
