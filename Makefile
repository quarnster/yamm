JAVAC = javac

.SUFFIXES: .class .java
.java.class:
	$(JAVAC) -g $<

CLASSES = org/gjt/fredde/util/UUDecoder.class \
	org/gjt/fredde/util/UUEncoder.class \
	org/gjt/fredde/util/net/Pop3.class \
	org/gjt/fredde/util/net/Smtp.class \
	org/gjt/fredde/util/net/Browser.class \
        org/gjt/fredde/util/gui/MsgDialog.class \
        org/gjt/fredde/util/gui/ExceptionDialog.class \
        org/gjt/fredde/util/gui/statusRow.class \
        org/gjt/fredde/util/gui/SplashScreen.class \
	org/gjt/fredde/yamm/gui/AttachListRenderer.class \
	org/gjt/fredde/yamm/gui/imageViewer.class \
	org/gjt/fredde/yamm/gui/sourceViewer.class \
	org/gjt/fredde/yamm/encode/Base64Decode.class \
	org/gjt/fredde/yamm/encode/UUDecode.class \
	org/gjt/fredde/yamm/encode/UUEncode.class \
	org/gjt/fredde/yamm/mail/Attachment.class \
	org/gjt/fredde/yamm/mail/MessageHeaderParser.class \
	org/gjt/fredde/yamm/mail/MessageBodyParser.class \
	org/gjt/fredde/yamm/mail/DateParser.class \
	org/gjt/fredde/yamm/mail/MessageParser.class \
	org/gjt/fredde/yamm/mail/ReplyMessageParser.class \
	org/gjt/fredde/yamm/mail/ReplyMessageBodyParser.class \
	org/gjt/fredde/yamm/mail/DateParser.class \
	org/gjt/fredde/yamm/mail/Mailbox.class \
	org/gjt/fredde/yamm/mail/Filter.class \
	org/gjt/fredde/yamm/gui/main/extMItem.class \
	org/gjt/fredde/yamm/gui/BoxTreeRenderer.class \
	org/gjt/fredde/yamm/gui/main/mainToolBar.class \
	org/gjt/fredde/yamm/gui/main/mainJTree.class \
	org/gjt/fredde/yamm/gui/main/mainTable.class \
	org/gjt/fredde/yamm/gui/main/mainMenu.class \
	org/gjt/fredde/yamm/SHMail.class \
	org/gjt/fredde/yamm/Options.class \
	org/gjt/fredde/yamm/YAMMWrite.class \
	org/gjt/fredde/yamm/YAMM.class


all: YAMM

clean:
	rm -f org/gjt/fredde/yamm/*.class
	rm -f org/gjt/fredde/yamm/encode/*.class
	rm -f org/gjt/fredde/yamm/mail/*.class
	rm -f org/gjt/fredde/yamm/gui/*.class
	rm -f org/gjt/fredde/yamm/gui/main/*.class
	rm -f org/gjt/fredde/util/net/*.class
	rm -f org/gjt/fredde/util/gui/*.class
	rm -f org/gjt/fredde/util/*.class

YAMM: $(CLASSES)
