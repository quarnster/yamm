JAVAC = javac

.SUFFIXES: .class .java
.java.class:
	$(JAVAC) $<

CLASSES = org/gjt/fredde/yamm/YAMM.class \
	org/gjt/fredde/yamm/YAMMWrite.class \
	org/gjt/fredde/yamm/Options.class \
	org/gjt/fredde/yamm/SHMail.class \
	org/gjt/fredde/yamm/Print.class \
	org/gjt/fredde/yamm/gui/imageViewer.class \
	org/gjt/fredde/yamm/gui/pDialog.class \
	org/gjt/fredde/yamm/gui/sourceViewer.class \
	org/gjt/fredde/yamm/gui/main/mainMenu.class \
	org/gjt/fredde/yamm/gui/main/mainToolBar.class \
	org/gjt/fredde/yamm/gui/main/mainJTree.class \
	org/gjt/fredde/yamm/gui/main/mainTable.class \
	org/gjt/fredde/yamm/gui/main/extMItem.class \
	org/gjt/fredde/yamm/mail/Mailbox.class \
	org/gjt/fredde/yamm/mail/Filter.class \
	org/gjt/fredde/yamm/encode/Base64Decode.class \
	org/gjt/fredde/yamm/encode/UUDecode.class \
	org/gjt/fredde/yamm/encode/UUEncode.class \
	org/gjt/fredde/util/net/Pop3.class \
	org/gjt/fredde/util/net/Smtp.class \
        org/gjt/fredde/util/gui/MsgDialog.class \
        org/gjt/fredde/util/gui/SplashScreen.class

all: YAMM

clean:
	rm -f org/gjt/fredde/yamm/*.class          org/gjt/fredde/yamm/*~
	rm -f org/gjt/fredde/yamm/encode/*.class   org/gjt/fredde/yamm/encode/*~
	rm -f org/gjt/fredde/yamm/mail/*.class     org/gjt/fredde/yamm/mail/*~
	rm -f org/gjt/fredde/yamm/gui/*.class      org/gjt/fredde/yamm/gui/*~
	rm -f org/gjt/fredde/yamm/gui/main/*.class org/gjt/fredde/yamm/gui/main/*~
	rm -f org/gjt/fredde/util/net/*.class      org/gjt/fredde/util/net/*~
	rm -f org/gjt/fredde/util/gui/*.class      org/gjt/fredde/util/gui/*~

YAMM: $(CLASSES)
