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
	org/gjt/fredde/yamm/SHMail.class \
	org/gjt/fredde/yamm/YAMMWrite.class \
	org/gjt/fredde/yamm/YAMM.class \
	org/gjt/fredde/yamm/YammPop3.class \
	org/gjt/fredde/yamm/YammSmtp.class \
	org/gjt/fredde/yamm/Utilities.class \
	org/gjt/fredde/yamm/Profile.class \
	org/gjt/fredde/yamm/Profiler.class \
	org/gjt/fredde/yamm/gui/AttachListRenderer.class \
	org/gjt/fredde/yamm/gui/BoxTreeRenderer.class \
	org/gjt/fredde/yamm/gui/MailTableRenderer.class \
	org/gjt/fredde/yamm/gui/TreeTableCellRenderer.class \
	org/gjt/fredde/yamm/gui/imageViewer.class \
	org/gjt/fredde/yamm/gui/sourceViewer.class \
	org/gjt/fredde/yamm/gui/confwiz/ConfigurationWizard.class \
	org/gjt/fredde/yamm/gui/confwiz/GeneralConfTab.class \
	org/gjt/fredde/yamm/gui/confwiz/IdentitiesConfTab.class \
	org/gjt/fredde/yamm/gui/confwiz/ServersConfTab.class \
	org/gjt/fredde/yamm/gui/confwiz/DebugConfTab.class \
	org/gjt/fredde/yamm/gui/confwiz/ControlPanel.class \
	org/gjt/fredde/yamm/gui/confwiz/ServerEditor.class \
	org/gjt/fredde/yamm/gui/confwiz/ProfileEditor.class \
	org/gjt/fredde/yamm/gui/confwiz/FiltersConfTab.class \
	org/gjt/fredde/yamm/gui/confwiz/FilterEditor.class \
	org/gjt/fredde/yamm/encode/Base64Decode.class \
	org/gjt/fredde/yamm/encode/Base64Encode.class \
	org/gjt/fredde/yamm/encode/Encoder.class \
	org/gjt/fredde/yamm/encode/Decoder.class \
	org/gjt/fredde/yamm/encode/Mime.class \
	org/gjt/fredde/yamm/encode/Html.class \
	org/gjt/fredde/yamm/encode/Base64sun.class \
	org/gjt/fredde/yamm/encode/UUDecode.class \
	org/gjt/fredde/yamm/encode/UUEncode.class \
	org/gjt/fredde/yamm/mail/Attachment.class \
	org/gjt/fredde/yamm/mail/MessageHeaderParser.class \
	org/gjt/fredde/yamm/mail/DateParser.class \
	org/gjt/fredde/yamm/mail/MessageParser.class \
	org/gjt/fredde/yamm/mail/DateParser.class \
	org/gjt/fredde/yamm/mail/Mailbox.class \
	org/gjt/fredde/yamm/mail/Filter.class \
	org/gjt/fredde/yamm/gui/main/extMItem.class \
	org/gjt/fredde/yamm/gui/main/mainToolBar.class \
	org/gjt/fredde/yamm/gui/main/mainJTree.class \
	org/gjt/fredde/yamm/gui/main/mainTable.class \
	org/gjt/fredde/yamm/gui/main/mainMenu.class \
	org/gjt/fredde/yamm/gui/main/NewBoxDialog.class \
	org/gjt/fredde/yamm/gui/main/NewGroupDialog.class


all: YAMM

jar: YAMM
	jar cmvf MANIFEST.MF yamm.jar \
	org/gjt/fredde/yamm/*.class \
	org/gjt/fredde/yamm/gui/*.class \
	org/gjt/fredde/yamm/gui/confwiz/*.class \
	org/gjt/fredde/yamm/gui/main/*.class \
	org/gjt/fredde/yamm/mail/*.class \
	org/gjt/fredde/yamm/encode/*.class \
	org/gjt/fredde/util/*.class \
	org/gjt/fredde/util/gui/*.class \
	org/gjt/fredde/util/net/*.class \
	YAMM*.properties \
	images/

clean:
	rm -f org/gjt/fredde/yamm/*.class org/gjt/fredde/yamm/*~
	rm -f org/gjt/fredde/yamm/encode/*.class org/gjt/fredde/yamm/encode/*~
	rm -f org/gjt/fredde/yamm/mail/*.class org/gjt/fredde/yamm/mail/*~
	rm -f org/gjt/fredde/yamm/gui/*.class org/gjt/fredde/yamm/gui/*~
	rm -f org/gjt/fredde/yamm/gui/main/*.class org/gjt/fredde/yamm/gui/main/*~
	rm -f org/gjt/fredde/yamm/gui/confwiz/*.class org/gjt/fredde/yamm/gui/confwiz/*~
	rm -f org/gjt/fredde/util/net/*.class org/gjt/fredde/util/net/*~
	rm -f org/gjt/fredde/util/gui/*.class org/gjt/fredde/util/gui/*~
	rm -f org/gjt/fredde/util/*.class org/gjt/fredde/util/*~

YAMM: $(CLASSES)
