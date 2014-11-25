LaTeXLabeler
============
A tool to label chapters, sections & subsections in LaTeX.
Author: Rik Claessens (www.rikclaessens.nl)

#### General structure of the LaTeX:

The top level folders each will contain the name of the LaTeX project. Below I always use the following structure:

	[paper name XXXX]
	├── classname.cls					// the LaTeX class
	├── doc.tex 						// the general LaTeX doc, this contains \begin{document}
	└── references
		├── articles.bib				// bib file containing references to papers and articles
		├── books.bib					// bib file containing references to books
		├── web.bib					// bib file containing references to news items on the internet
		└── bibliography.tex			// LaTeX file that prints the bibliography
	├── resources						// LaTeX files that are used for tables, plots etc.
	├── sections						// The actual body of the paper, holds a LaTeX file per section
		├── section1.tex				// Contents of the sections
		├── section2.tex
		└── section3.tex
	├── settings						// A collection of LaTeX settings, including acronyms, authors, variables, tikz settings
	├── tikz							// Folder in which tikz images are created and built
		├──	image1					// Folder that contains a single tikz image
			├── image1doc.tex			// LaTeX file that is used to build the tikz image
			├── image1.tex			// LaTeX file that containts the tikz image
			└── image1.pdf			// Generated pdf file of the image, this is inserted into the paper
	└── tikzlibrarybayesnet.code.tex	// Library to create Bayesian networks etc.

By running the following command all relevant LaTeX tags will be labelled:

	java -jar /path/to/.../LaTeXLabeler.jar "/path/to/top level folder/" "doc.tex"

The LaTeX tags are labelled with a short version of the tag type (e.g. sec or subsec) followed by the name of the tag. You'll end up with something like this:

	\section{Introduction}
	\label{sec:introduction}

There are some glitches when a file ends with a LaTeX tag, but it's easy to spot since there will be two headings in this case.

#### How to build a publication using LaTeX
I'm using Sublime Text 3 in combination with LaTeXTools myself. But it is possible to use any editor and build a publication with the following command from the command line inside one of the top level folders:

	pdflatex <doc>.tex

The images in the **/tikz** folder are externalized to pdfs using *tikzexternalize*. If the image does not require the **fontspec** package then use the command:

	pdflatex --shell-escape <doc>.tex

Otherwise you need to use XeLaTeX:

	xelatex -8bit -shell-escape <doc>.tex

#### Other requirements
###### FontAwesome
For some of the externalized images I'm using an icon font called [FontAwesome](http://fontawesome.io). You need to download the package and install the **.ttf** files on your system before you will be able to build some of the images. If this doesn't work, just shoot me a message and I will sort it out ASAP.mark
