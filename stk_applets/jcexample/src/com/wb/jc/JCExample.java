/*
 * JCExample
 * SIM Toolkit / Java Card example for Bachelors Thesis in Computer Engineering.
 * SIM card target type is R99.
 * Code was written by Peter Edsbäcker 2010, 2011.
 * Based on example skeleton code provided by Gemalto's Applet wizard.
 */
package com.wb.jc; // Applet's package name
/*
 * Imported packages
 */
import sim.toolkit.*;
import sim.access.*;
import javacard.framework.*;

public class JCExample extends Applet implements ToolkitInterface, ToolkitConstants
{
    private static final short MSG_MAINMENU_OFFSET = (short) 0;
    private static final short MSG_MAINMENU_LENGTH = (short) 9;
    private static final short MSG_NUMBER_TO_DIAL_OFFSET = (short) MSG_MAINMENU_LENGTH;
    private static final short MSG_NUMBER_TO_DIAL_LENGTH = (short) 14; // "Number to dial"
    private static final short MSG_NUMBER_TO_SMS_OFFSET = (short) (MSG_NUMBER_TO_DIAL_OFFSET+MSG_NUMBER_TO_DIAL_LENGTH);
    private static final short MSG_NUMBER_TO_SMS_LENGTH = (short) 16; // "Number to SMS to"

    private static final short MSG_DIALING_OFFSET = (short) (MSG_NUMBER_TO_SMS_OFFSET+MSG_NUMBER_TO_SMS_LENGTH);
    private static final short MSG_DIALING_LENGTH = (short) 7; // "Dialing"
    private static final short MSG_SENDING_SMS_OFFSET = (short) (MSG_DIALING_OFFSET+MSG_DIALING_LENGTH);
    private static final short MSG_SENDING_SMS_LENGTH = (short) 11; // "Sending SMS"

    private static final short MSG_SUCCESS_OFFSET = (short) (MSG_SENDING_SMS_OFFSET+MSG_SENDING_SMS_LENGTH);
    private static final short MSG_SUCCESS_LENGTH = (short) 7; // "Success"
    private static final short MSG_FAILURE_OFFSET = (short) (MSG_SUCCESS_OFFSET+MSG_SUCCESS_LENGTH);
    private static final short MSG_FAILURE_LENGTH = (short) 7; // "Failure"

    private static final short MSG_MENU_OFFSET = (short) (MSG_FAILURE_OFFSET+MSG_FAILURE_LENGTH);
    private static final short MSG_MENU_LENGTH = (short) 4; // "Menu"
    private static final short MSG_DIAL_NUMBER_OFFSET = (short) (MSG_MENU_OFFSET+MSG_MENU_LENGTH);
    private static final short MSG_DIAL_NUMBER_LENGTH = (short) 13; // "1 Dial number"
    private static final short MSG_SEND_SMS_OFFSET = (short) (MSG_DIAL_NUMBER_OFFSET+MSG_DIAL_NUMBER_LENGTH);
    private static final short MSG_SEND_SMS_LENGTH = (short) 10; // "2 Send SMS"

    private static final short MSG_EXIT_OFFSET = (short)
        (MSG_SEND_SMS_OFFSET+MSG_SEND_SMS_LENGTH);
    private static final short MSG_EXIT_LENGTH = (short) 6; // "3 Exit"

    // SMSC dialing number protocol constant
    private static final byte SMS_TON_NPI = (byte) 0x081; // Local number, NPI ISDN/telephone numbering plan.
    private static final byte VOICECALL_TON_NPI = (byte) 0x081; // Same here.
    private static final byte GI_DIGITSONLY = 0x008;
    private static final byte GI_HIDEINPUT = 0x004;

    // Byte buffer containing the Applet's data strings
    // We pack everything together to save space. For a real application this might
    // be created by a pre-compiler or exist in a GSM file (for multiple languages)
    //
    private byte[] messageStrings =
    {
        // "JCExample"
        (byte)'J',(byte)'C',(byte)'E',(byte)'x',(byte)'a',(byte)'m',
        (byte)'p',(byte)'l',(byte)'e',
        // "Number to dial" (14)
        (byte)'N',(byte)'u',(byte)'m',(byte)'b',(byte)'e',(byte)'r', (byte)' ',
        (byte)'t',(byte)'o',(byte)' ',(byte)'d',(byte)'i',(byte)'a', (byte)'l',
        // "Number to SMS to" (16)
        (byte)'N',(byte)'u',(byte)'m',(byte)'b',(byte)'e',(byte)'r', (byte)' ',
        (byte)'t',(byte)'o',(byte)' ',(byte)'S',(byte)'M',(byte)'S', (byte)' ',
        (byte)'t',(byte)'o',

        // "Dialing" (7)
        (byte)'D',(byte)'i',(byte)'a',(byte)'l',(byte)'i', (byte)'n',(byte)'g',
        // "Sending SMS" (11)
        (byte)'S',(byte)'e',(byte)'n',(byte)'d',(byte)'i',(byte)'n', (byte)'g', (byte)' ',
        (byte)'S',(byte)'M',(byte)'S',

        // "Success" (7)
        (byte)'S',(byte)'u',(byte)'c',(byte)'c',(byte)'e',(byte)'s', (byte)'s',
        // "Failure" (7)
        (byte)'F',(byte)'a',(byte)'i',(byte)'l',(byte)'u',(byte)'r', (byte)'e',

        // "Menu" (4)
        (byte)'M',(byte)'e',(byte)'n',(byte)'u',

        // "1 Dial number" (13)
        (byte)'1',(byte)' ',(byte)'D',(byte)'i',(byte)'a',(byte)'l', (byte)' ',
        (byte)'n',(byte)'u',(byte)'m',(byte)'b',(byte)'e',(byte)'r',

        // "2 Send SMS" (10)
        (byte)'2',(byte)' ',(byte)'S',(byte)'e',(byte)'n',(byte)'d', (byte)' ',
        (byte)'S',(byte)'M',(byte)'S',

        // "3 Exit" (6)
        (byte)'3',(byte)' ',(byte)'E',(byte)'x',(byte)'i',(byte)'t'
    };

    // Has to be in its own buffer for now.
    private byte[] MessageSendSMS =
    {
        // "Sending SMS"
        (byte)'S',(byte)'e',(byte)'n',(byte)'d',(byte)'i',(byte)'n', (byte)'g',
        (byte)' ', (byte)'S',(byte)'M',(byte)'S',
    };

    private byte[] MessageSMSBodyText =
    {
        // SIM card says hello!
        (byte)'S',(byte)'I',(byte)'M',(byte)' ',(byte)'C',(byte)'a', (byte)'r',
        (byte)'d',(byte)' ',(byte)'s',(byte)'a',(byte)'y',(byte)'s',(byte)' ',
        (byte)'h',(byte)'e',(byte)'l',(byte)'l',(byte)'o',(byte)'!'
    };


    private short[] MainMenu =
    {
        // First comes menu's title (ofs,len)
        MSG_MENU_OFFSET, MSG_MENU_LENGTH,

        // Then comes the items (ofs,len)
        MSG_DIAL_NUMBER_OFFSET, MSG_DIAL_NUMBER_LENGTH,
        MSG_SEND_SMS_OFFSET, MSG_SEND_SMS_LENGTH,
        MSG_EXIT_OFFSET, MSG_EXIT_LENGTH
    };



    // Mask defining the SIM Toolkit features required by the Applet
    // It is a bitmask with 1s reflecting the needed profiles.
    //
    private byte[] terminalProfileMask =
    {(byte)0x09,(byte)0x03,(byte)0x21,(byte)0x70,(byte)0x0D};
    // Volatile RAM temporary buffer for storing intermediate data and results
    // It is 180 bytes, enough for a long SMS + dialing number.
    //
    private byte[] tempBuffer;
    private boolean environmentOk = false;
    private boolean eventsRegistered;
    private byte menuEntryId_1; // SIM Root menu item id. Used for register/unregister

    /**
     * Constructor of the Applet
     * It is only executed during Applet installation.
     * All memory buffers should be created here (at least for the
     * JavaCard 2.x generation)
     */
    public JCExample()
    {
        // Create tempBuffer[] in RAM (to avoid EEPROM stress due to high update rates)
        tempBuffer = JCSystem.makeTransientByteArray((short)180, JCSystem.CLEAR_ON_RESET);
        // Register to the SIM Toolkit Framework
        ToolkitRegistry reg = ToolkitRegistry.getEntry();
        // Register the Applet under the EVENT_MENU_SELECTION event
        // PRO_CMD_SET_UP_CALL here means gurka
        //
        menuEntryId_1 = reg.initMenuEntry(
                messageStrings, MSG_MAINMENU_OFFSET, MSG_MAINMENU_LENGTH,
                PRO_CMD_SET_UP_CALL,
                false, (byte)0, (short)0);
        // Register the other events usd by the Applet
        reg.setEvent(EVENT_CALL_CONTROL_BY_SIM);
        reg.setEvent(EVENT_PROFILE_DOWNLOAD);
        // Set the 'eventsRegistered' flag if there is no exception before it.
        eventsRegistered = true;
    }

    /**
     * Method called by the JCRE when the Applet is installed
     * The bArray contains installation parameters
     * These usable parameters are stored as LV-pairs (1 byte=Data length followed by
     parameter data)
     * and follow in this order (data pairs starts at offset <bOffset>)
     * - Applet's instance AID
     * - Control information
     * - Applet data (the "command line" for the Applet instance)
     * If you want to fail installation please throw an exception with
     * ISO7816.throwit(exception_constant), see Java Card documentation.
     */
    public static void install(byte bArray[], short bOffset, byte bLength)
    {
        // Create the Applet instance
        JCExample JavaCardExample = new JCExample();

        // Register the Applet's instance with the JCRE.
        // Argument here is the AID parameter of the installation parameters (as seen above)
        //
        JavaCardExample.register(bArray, (short)(bOffset + 1), (byte)bArray[bOffset]);
    }


    /**
     * Method called by the SIM Toolkit Framework to trigger the Applet
     */
    public void processToolkit(byte event)
    {
        // Define the SIM Toolkit session handler variables
        // We don't fetch unused values directly here, everything takes extra time..
        //
        EnvelopeHandler envHdlr;
        ProactiveHandler proHdlr;
        ProactiveResponseHandler rspHdlr;

        EnvelopeResponseHandler envRspHdlr;
        switch(event)
        {
            // The EVENT_PROFILE_DOWNLOAD happens after installation here you can check
            // if the SIM card and phones (ME) functionality is good enough for your application.
                //
            case EVENT_PROFILE_DOWNLOAD:
                // Test that Mobile Equipment capabilities and card personalisation are compatible
                // with the Applet's requirements
                environmentOk = testAppletEnvironment();
                if (environmentOk) // ME capabilities and SIM are OK.
                {
                    // Test if Applet events are registered and register if necessary
                    if (!eventsRegistered)
                        registerEvents(); // Applet can now respond to events and shows in root menu.
                }
                else
                {
                    if (eventsRegistered)
                        clearEvents(); // Applet no longer receives events and does not show in root menu
                }
                break;
                // The EVENT_STATUS_COMMAND is used so Applet gets called
                // at specified intervals. We don't use it in this example.
                // case EVENT_STATUS_COMMAND:
                // break;
                // Example of call control
                // This event is generated when the ME is about to dial a number.
                // The SIM can change dialling parameters (including phone number)
                // before dialling actually takes place. Here we just return "OK"
                //
            case EVENT_CALL_CONTROL_BY_SIM:
                envRspHdlr = EnvelopeResponseHandler.getTheHandler();
                envRspHdlr.postAsBERTLV((byte)0x9F, (byte)0x00); // (0x00 is a result code)
                    break;

            case EVENT_EVENT_DOWNLOAD_CALL_CONNECTED:
                // Call connected. Here you could for example send DTMF tones
                // with command PRO_CMD_SEND_DTMF (ETSI section 11.11)
                // It is NOT typically supported to display messages etc here on most phones!!
                    break;

                // EVENT_MENU_SELECTION : Root menu selection done
                //
            case EVENT_MENU_SELECTION:
                // Get the references of the required SIM Toolkit session handlers
                proHdlr = ProactiveHandler.getTheHandler();
                envHdlr = EnvelopeHandler.getTheHandler();
                // Get the identifier of the SIM Tooklit menu item selected by the user
                byte itemId = envHdlr.getItemIdentifier();
                // If selected item identifier matches registered menu item identifier ...
                if (itemId == menuEntryId_1)
                {
                    RootMenuHandler();
                }
                break;
        }
    }

    /**
     * Method called by the JCRE, once selected
     */
    public void process(APDU apdu)
    {
        // Method not implemented - this Applet only processes SIM Toolkit events
    }

    public void RootMenuHandler()
    {

        while(true)
        {
            byte item = displayMenu(messageStrings, MainMenu, (byte)0);
            if (item<0 || item >= 2) // Error or "exit" selected?
                break;

            switch (item)
            {
                case 0: // Dial number.
                    AskForInputAndDialNumber();
                    break;

                case 1: // Send SMS
                    AskForInputAndSendTestSMS();
                    break;
            }
        }
    }

    private void AskForInputAndDialNumber()
    {
        short len = getInput(messageStrings, MSG_NUMBER_TO_DIAL_OFFSET,
                MSG_NUMBER_TO_DIAL_LENGTH,
                tempBuffer, (short)0, // Result into tempBuffer at offset 0.
                (short)1, (short)16, GI_DIGITSONLY); // Min 1 digit, Max 16 digits.

        if (len > 0) // Valid user respose.
        {
            // Dial number in tempBuffer, connect, wait until disconnect.
            // Note that packed data is placed in workBuffer after offset len
            //
            boolean success = dialNumber((short)0, len);
            DisplayOperationResult(success);
        }
    }


    private void AskForInputAndSendTestSMS()
    {
        short len = getInput(messageStrings, MSG_NUMBER_TO_SMS_OFFSET,
                MSG_NUMBER_TO_SMS_LENGTH,
                tempBuffer, (short)0, // Result into tempBuffer at offset 0.
                (short)1, (short)16, GI_DIGITSONLY); // Min 1 digit, Max 16 digits.

        if (len > 0) // Valid user respose.
        {
            short rc = sendSMS(
                    true, // Send as ASCII 7-bit (standard 160 character limit).
                    MessageSMSBodyText, (short)0, (short)MessageSMSBodyText.length, // Text to send to target.
                    tempBuffer, // tempBuffer contains SMS dialling number.
                    (short) 0, // tempBuffer offset for SMS number.
                    len, // tempBuffer number length
                    MessageSendSMS, // Message to display while dispatching SMS (from ofs 0, length=.length)
                    tempBuffer,
                    len); // Transient data buffer, at least 160 characters after ofs "len" (16+160)

            DisplayOperationResult(rc>=0);
        }
    }
    /**
     * Displays "success" or "failure" to the user.
     * @param displaySuccess
     */
    private void DisplayOperationResult(boolean displaySuccess)
    {
        if (displaySuccess)
            displayMsgWaitRsp(messageStrings, MSG_SUCCESS_OFFSET, MSG_SUCCESS_LENGTH);
        else
            displayMsgWaitRsp(messageStrings, MSG_FAILURE_OFFSET,
                    MSG_FAILURE_LENGTH);
    }


    /**
     * Tests whether :
     * - the Mobile Equipment supports the SIM Toolkit functionalities required by the
     Applet.
     * - the card's file system contains the files required by the Applet.
     *
     * Returns :
     * - boolean result : true if the ME and card comply with the Applet's requirements,
     * false otherwise.
     */
    private boolean testAppletEnvironment()
    {
        // Check that the ME (phone) supports the toolkit features required by the Applet.
        // The .check method with one argument can be used to see if the ME (phone) support certain command(s)
            // (For example the PRO_CMD_xxx constants)
            // Here we use a bit-mask array to check multiple features at once,
            // we expect features with respective bit set to "1" to exist.
            // Bit-0 in the array refers to existence of function 0 and so on.
            //
            return (MEProfile.check(
                        terminalProfileMask, (short)0, (short)terminalProfileMask.length)
                   );
    }

    /**
     * Registers the events used by the Applet (excepting EVENT_PROFILE_DOWNLOAD which is
     * not cleared) and sets the eventsRegistered flag to 'true'.
     */
    private void registerEvents ()
    {
        ToolkitRegistry reg = ToolkitRegistry.getEntry();
        // Enable EVENT_MENU_SELECTION for menuEntryId_1
        reg.enableMenuEntry(menuEntryId_1);
        // Register EVENT_STATUS_COMMAND every 30 sec (not used)
        // reg.requestPollInterval((short)30);
        // Register all the other events we listen to in the Applet.
        //
        reg.setEvent(EVENT_CALL_CONTROL_BY_SIM); // When ME is about to dial.
        // reg.setEvent(EVENT_EVENT_DOWNLOAD_CALL_CONNECTED); // When ME has connected.
        // reg.setEvent(EVENT_EVENT_DOWNLOAD_CALL_DISCONNECTED); // When ME has disconnected.
            // Set the eventsRegistered flag (no exception happened before it)
            eventsRegistered = true;
    }
    /**
     * Clears the events used by the Applet (excepting EVENT_PROFILE_DOWNLOAD so that
     * the Applet can continue testing its environment and register the events if a
     * compliant environment is detected).
     * Also sets the eventsRegistered flag to 'false'.
     */
    private void clearEvents ()
    {
        ToolkitRegistry reg = ToolkitRegistry.getEntry();
        // Disable EVENT_MENU_SELECTION for menuEntryId_1
        reg.disableMenuEntry(menuEntryId_1);
        // Clear EVENT_STATUS_COMMAND (Not used)
        // reg.requestPollInterval(POLL_NO_DURATION);
        // Clear all the other events used by the Applet
        reg.clearEvent(EVENT_CALL_CONTROL_BY_SIM);

        // Call progress monitoring not implemented in this example:
        // reg.clearEvent(EVENT_EVENT_DOWNLOAD_CALL_CONNECTED);
        // reg.clearEvent(EVENT_EVENT_DOWNLOAD_CALL_DISCONNECTED);
        // Set the eventsRegistered to false
        eventsRegistered = false;
    }
    /**
     * 
     * @param send_7bit : Send ASCII 7-bit (standard 160 character limit). Otherwise 8-bit
     (140 char limit)
     * @param databuff : Buffer to send
     * @param databuff_ofs : Offset in send buffer
     * @param databuff_len : Length of data to send
     * @param smsc_address_data : SMSC address buffer, digit as byte value 0-9 (SMS number
     destination)
     * @param smsc_address_ofs : Offset in buffer
     * @param smsc_address_len : Length of SMSC number.
     * @param sending_user_message : Text to display to the user while sending
     * @param work_buffer : Transient work buffer, at least 160
     bytes+4+smsc_address_length/2
     * @return
     */
    public short sendSMS(
            boolean send_7bit, // Send ASCII 7-bit (standard 160 character limit).  Otherwise 8-bit (140 char limit)
            byte[] sms,
            short smsOffset,
            short smsLength,
            byte[] smsc_address_data,
            short smsc_address_ofs,
            short smsc_address_len,
            byte[] userMessageDuringSending, // Message to display while dispatching SMS (from ofs 0, length=.length)
            byte[] workBuffer,
            short workBufferOffset) // Transient data buffer, at least 160 characters+
    {
        ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();
        short rc;
        short pos = workBufferOffset;
        byte tpdu_dcs;

        final byte CMD_QUALIFIER_PACKING_NOT_REQUIRED = (byte) 0;
        // Now Construct the SMS TPDU in the recordData
        // see GSM 03.40 for details
        workBuffer[pos] = (byte) 0x01; // SMS-Submit Type
        workBuffer[(short)(pos+1)] = (byte) 0x01; // Message reference
        pos+= 2;
        // TP-Destination Address
        //
        short adn_length = buildADNumber(
                true,
                workBuffer,
                pos, // Add encoded address at offset 2.
                smsc_address_data, smsc_address_ofs, smsc_address_len );
        pos+= adn_length;
        workBuffer[pos++] = (byte) 0x00; // TP-PID 0x00 (Protocol identifier)
        tpdu_dcs = (byte)0x00;
        if ( !send_7bit )
            tpdu_dcs = (byte) 0x04; // 8bit data
        // DCS
        // Bit5 : 1=compressed [0x020]
        // Bit4 : 1=Use bit1,0 as message class, 0=bit1,0 is reserved [0x010]
        // Bit3,2: Alphabet (0=GSM 7bit,1=8bit,2=UCS2,3=reserved) [0x008,0x004]
            // Bit1,0: 0=Class0, 1=MobilePhoneSpecific, 2=SIM specific, 3=TE specific
            // Class0 is "Flash SMS" and are not normally stored by the phone.
            //
            workBuffer[pos++] = tpdu_dcs; // TP-DCS 0x00 (Data coding scheme)
        workBuffer[pos++] = (byte) smsLength; // (byte) (smstpduLength - start_offset);
        // Pack data to 7 bit before sending
        if ( send_7bit )
        {
            short acc = 0;
            short bit_index=0;
            for(short si=0; si<smsLength; si++)
            {
                acc|= ((sms[(short)(si+smsOffset)]&0x0ff) << bit_index);
                // Highest allowed is 7
                bit_index+= 7;
                if ( bit_index >= 8 )
                {
                    workBuffer[pos++] = (byte) acc;
                    acc>>>= 8; // Unsigned shift right high. Do NOT allow high sign bit to spread!
                        bit_index&= 0x07; // Mask bit index to get new current index.
                }
            }
            if ( bit_index != 0 )
            {
                if ( bit_index == 1 ) // Can exactly fit one extra byte, it has to be a space..
                        acc = (short)((short)' ' << bit_index);
                workBuffer[pos++] = (byte) acc;
            }
        }
        else
        {
            Util.arrayCopyNonAtomic(sms, (short)smsOffset, workBuffer, pos,
                    smsLength);
            pos+= smsLength;
        }
        try
        {
            // Sending the message, command 19 = 0x013
            proHdlr.init(PRO_CMD_SEND_SHORT_MESSAGE,CMD_QUALIFIER_PACKING_NOT_REQUIRED,
                    DEV_ID_NETWORK);
            if ( userMessageDuringSending != null )
                proHdlr.appendTLV((byte)
                        (TAG_ALPHA_IDENTIFIER),userMessageDuringSending, (byte)0, (short)
                        userMessageDuringSending.length);

            // FIXME - navi commented this code
            // proHdlr.appendTLV((byte) (TAG_SMS_TPDU), workBuffer, workBufferOffset, (short)(posworkBufferOffset));
            rc = proHdlr.send();
        }
        catch(Exception ex)
        {
            rc = (int) -256;
        }
        return rc;
    }
    /**
     * Dials given number.
     * Places packed temporary data in tempBuffer after
     * position numberTempBufferOffset+numberLength.
     *
     * Before dialing the ME creates a EVENT_CALL_CONTROL_BY_SIM event
     * where we can adjust dialing parameters before actual dial is done.
     * Typically the call ends up with these events:
     * EVENT_EVENT_DOWNLOAD_CALL_CONNECTED
     * followed by a
     * EVENT_EVENT_DOWNLOAD_CALL_DISCONNECTED
     */
    public boolean dialNumber(short numberTempBufferOffset, short numberLength)
    {
        ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();
        // Put the packed BCD data into "tempBuffer" after given input string.
        short packed_offset = (short)(numberTempBufferOffset + numberLength);
        short packed_len = buildADNumber(false, tempBuffer, packed_offset,
                tempBuffer, numberTempBufferOffset, numberLength);

        // See GSM 11.14 spec, "SET UP CALL" section 6.4.13 and 6.6.12
        // The init command creates a BER-TLV with a "command details" tag
        // added automatically (see section 11.6). Here 0x00 means "call only if
        // no other call is in progress". DEV_ID_NETWORK is the command qualifier byte.
            //
            proHdlr.init(PRO_CMD_SET_UP_CALL, (byte)0x00, DEV_ID_NETWORK);

        proHdlr.appendTLV(TAG_ALPHA_IDENTIFIER, messageStrings, MSG_DIALING_OFFSET,
                MSG_DIALING_LENGTH);
        proHdlr.appendTLV(TAG_ADDRESS, tempBuffer, packed_offset, packed_len); // TON/NPI byte followed by packed BCD number
            proHdlr.send();

        ProactiveResponseHandler prh = ProactiveResponseHandler.getTheHandler();
        return ( prh.getGeneralResult() == 0 );
    }
    /**
     * buildADNumber
     * Creates a SMS/Dialling number from the input digits (bytes 0-9)
     * The output is in a packed BCD format (4 bits per digit)
     * Note: A NULL byte(0) or space ends the smsc_number, even if its array length is
     longer.
     * Returns: Length of generated data.
     */
    private static short buildADNumber(
            boolean buildSmscNumber, // If false it builds a phone number.
            byte[] dstBuffer,
            short dstBufferOffset,
            byte[] number, // 0-9 as ASCII BYTES.
            short numberOffset,
            short numberLength)
    {
        short inputDigits;
        for(inputDigits=0; inputDigits<numberLength; inputDigits++)
        {
            if ( number[(short)(numberOffset+inputDigits)] <= 32 )
                break; // Also break off if we see a space or 0
        }
        // Calculate length of BCD number in bytes.
        short result_length = (short)((short)(inputDigits+1) >> 1);
        if (buildSmscNumber)
        {
            result_length+= 2; // Two info header bytes.
            if (dstBuffer == null) // Just calculating length?
                return result_length;
            // 2 hdr bytes plus 4 bits per digit, padded with 0x0f if necessary (round size up)
                // See ETSI 102 / 8.1 Address, page 116.
                dstBuffer[dstBufferOffset++] = (byte) inputDigits;
            dstBuffer[dstBufferOffset++] = SMS_TON_NPI;
        }
        else
        {
            result_length++; // One header byte.
            if (dstBuffer == null) // Calculating length?
                return result_length;
            // For information about EF-ADN packed numbers see the GSM 11.11 document, section 11.1
                //
                dstBuffer[dstBufferOffset++] = VOICECALL_TON_NPI;
        }
        // Packed as 4 bit BCD, padded with 0x0f0 if needed (if unaligned data/uneven length)
            //
            short number_exit = (short)(numberOffset + inputDigits);
        while (numberOffset < number_exit)
        {
            byte data = (byte) (number[numberOffset++] - '0');
            dstBuffer[dstBufferOffset] = data;
            if (numberOffset >= number_exit) // Odd length and last digit!
            {
                dstBuffer[dstBufferOffset]|= 0x0f0; // Pad with BCD 0x0f
                break;
            }
            dstBuffer[dstBufferOffset++]|= (byte) ( (number[numberOffset++] -
                        '0') << 4 );

        }
        return result_length;
    }
    /**
     * Displays given message on the ME a few seconds (default time, see ETSI document)
     * @param msg : Buffer (8-bit GSM character set)
     * @param msgOffset : Offset of message in buffer
     * @param msgLength : Length of data
     */
    private void displayMessage(byte[] msg, short msgOffset, short msgLength)
    {
        ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();
        proHdlr.initDisplayText( (byte)0x00, DCS_8_BIT_DATA, msg, msgOffset,
                msgLength);
        proHdlr.send();
    }
    /* displayMsgWaitRsp (Command 21 decimal = 0x015)
     * RETURNS:
     * <0 : Error
     * 0 : User pressed NO
     * 1 : User pressed YES (OK etc)
     *
     * displayMessage is explained in (R5) TS 102 223 section 6.6.1
     * IMPORTANT: The QUALIFIER bits are described under 8.6!!!
     * DISPLAY TEXT:
     * bit 0: 0 = normal priority ; 1 = high priority.
     * bits 1 to 6: = Reserved
     * bit 7: 0 = clear message after a delay, 1 = wait for user to clear message.
     *
     * Duration of displayed message is explained in 8.8:
     * Byte(s) Description Length
     * 0 Duration tag 1
     * 1 Length = '02' 1
     * 2 Time unit 1 - '00' minutes , '01' seconds,
     '02' tenths of seconds, All other values are reserved.
     * 3 Time interval 1 - in units (0=reserved)
     *
     */
    private short displayMsgWaitRsp(byte[] msg, short pos, short len )
    {
        short rc;
        ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();
        proHdlr.initDisplayText((byte) 0x080, DCS_8_BIT_DATA, msg, pos, len );
        // FYI: This is how to do the same in "raw" mode
        // proHdlr.init( (byte) PRO_CMD_DISPLAY_TEXT, (byte) 0x81,(byte) DEV_ID_DISPLAY);
        // proHdlr.appendTLV( (byte) (TAG_TEXT_STRING), (byte) 0x04, msg, pos, len);
        if ( proHdlr.send() != 0 )
            return -2; // Command error.
        ProactiveResponseHandler prh = ProactiveResponseHandler.getTheHandler();
        rc = 0; // Responded NO (or some fishy key)
        if ( prh.getGeneralResult() == 0 )
            rc = 1; // Responded YES
        return rc; // Responded NO
    }
    /**
     * Get input from user.
     * @param menu_text
     * @param out_response
     * @param out_response_offset
     * @param minResponse
     * @param maxResponse
     * @param qualifier : The GI_xxx constants defined in the header.
     * @return
     */
    private short getInput(

            byte menuText[],
            short menuTextOffset,
            short menuTextLength,
            byte out_response[],
            short out_response_offset,
            short minResponse,
            short maxResponse,
            byte qualifier)
    {
        ProactiveHandler ph = ProactiveHandler.getTheHandler();
        // qualifier, ...
        ph.initGetInput(
                (byte) (qualifier & GI_HIDEINPUT),
                DCS_8_BIT_DATA,
                menuText, menuTextOffset, menuTextLength,
                minResponse,
                maxResponse);
        if ( ph.send() != 0 )
            return -1;
        ProactiveResponseHandler prh = ProactiveResponseHandler.getTheHandler();
        short out_responseLength = (byte)prh.getTextStringLength();
        prh.copyTextString(out_response, out_response_offset);
        if ( (qualifier & GI_DIGITSONLY)!=0 )
        {
            for(short i=0; i<out_responseLength; i++)
            {
                short bin_digit = (short) (
                        (out_response[(short)(i+out_response_offset)]-'0') & 0x0ff );
                if ( bin_digit > 9 )
                    return -2; // Invalid digits.
            }
        }
        return (out_responseLength);
    }
    /**
     * Defines a menu.
     * Data is tuples of (offset,length)
     * Tuple 0 is title
     * Tuple 1..N are the menu items, which gets menu index 0..N-1
     * @param menuDataBuffer
     * @param menuDefinition
     * @param selectedItemIndex if >=0, preselects given menu index.
     * @return
     */
    public byte displayMenu(
            byte[] menuDataBuffer,
            short[] menuDefinition,
            byte selectedItemIndex)
    {
        short menu_ofs, menu_exit, item_start_ofs, item_length;
        ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();
        // ---| PRE MENU SETUP
        proHdlr.init(PRO_CMD_SELECT_ITEM, (byte)0x080, DEV_ID_ME);
        // Menu header
        proHdlr.appendTLV(TAG_ALPHA_IDENTIFIER, menuDataBuffer, menuDefinition[0],
                menuDefinition[1]);
        short menu_data_offset = (short) 2;
        byte item_index = (byte) 0; // Added item's index. 0 = first item etc.
        // Menu items
        while (menu_data_offset < menuDefinition.length)
        {
            proHdlr.appendTLV((byte)(TAG_ITEM | TAG_SET_CR), item_index,
                    menuDataBuffer, // Item text buffer.
                    menuDefinition[menu_data_offset], // Offset
                    menuDefinition[(short)(menu_data_offset+1)]); // Length
            item_index++;

            menu_data_offset+= 2;
        }
        // pre-selected item (where the "cursor" is placed)
        if (selectedItemIndex>=0) // If we have a given start index >=0
            proHdlr.appendTLV( TAG_ITEM_IDENTIFIER, selectedItemIndex);
        if ((selectedItemIndex = proHdlr.send()) < 0)
            return selectedItemIndex; // Negative error codes.
        ProactiveResponseHandler prh = ProactiveResponseHandler.getTheHandler();
        if ( prh.getGeneralResult() != 0 )
            return -2; // Return -2 if general result fails.
        return (byte) prh.getItemIdentifier(); // becomes index 0..N-1
    }
}

