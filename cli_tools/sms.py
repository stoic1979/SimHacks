#
# SMS utils/wrappers
#
# Some part of code is taken from SIM Reader by Todd Whiteman, 
# of year 2005
#

from traceback import print_exc
from binascii import hexlify, unhexlify
import time, calendar
from utils import GSMPhoneNumberToString

#SMS_FILE_PATH = ["3F00", DF_TELECOM, EF_SMS]

STATUS_READ     = 0
STATUS_UNREAD   = 1
STATUS_DELETED  = 2

class SMSmessage:
    # SMS Deliver and SMS Submit
    # 0107911614910900F5040B911614836816F1 0000 2050107034146B
    # 4C    FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
    def __init__(self):
        self.status = 'Read'
        self.smsc = ''
        self.number = ''
        self.timestamp = ''
        self.timetuple = [0,0,0,0,0,0,0,0,0]
        self.message = ''

        self.mti  = 0
        self.mms  = 0
        self.sri  = 0
        self.udhi = 0
        self.rp   = 1
        self.pid  = 0
        self.dcs  = 0
        self.udl  = 0
        
        self.rawMessage = ''

    def clone(self):
        s = SMSmessage()
        s.smsToData(self.timestamp, self.number, self.smsc, self.message)
        s.rawMessage = self.rawMessage
        s.status = self.status
        return s

    def setStatus(self, val):
        if not (val & 0x1):
            self.status = "Deleted"
        elif not (val & 0x4):
            if not (val & 0x2):
                self.status = "Read"
            else:
                self.status = "Unread"
        elif (val & 0x7) == 0x7:
            self.status = "To be sent"
        else:
            self.status = "Unknown"

    def changeStatus(self, val=STATUS_READ):
        if val == STATUS_DELETED:
            i = 0
        elif val == STATUS_UNREAD:
            i = 0x3
        else:
            i = 0x1

        self.setStatus(i)
        if self.rawMessage:
            self.rawMessage = "0%d%s" % (i, self.rawMessage[2:])

    def smsFromData(self, data):
        self.rawMessage = data

        self.setStatus(int(data[0:2], 16))

        i = int(data[2:4], 16) << 1
        self.smsc = GSMPhoneNumberToString(data[4:4+i], replaceTonNPI=1)
        data = data[4+i:]

        val = int(data[0:2], 16)
        self.mti  = (val >> 6) & 3
        self.mms  = (val >> 5) & 1
        self.sri  = (val >> 4) & 1
        self.udhi = (val >> 3) & 1
        self.rp   = (val >> 2) & 1
        data = data[2:]

        i = int(data[:2], 16)
        j = 4 + i + (i % 2)
        self.number = GSMPhoneNumberToString(data[2:j], replaceTonNPI=1)
        data = data[j:]

        self.pid  = int(data[:2], 16)
        self.dcs  = int(data[2:4], 16)

        self.timestamp = self.convertTimestamp(data[4:18])

        self.udl  = int(data[18:20], 16) # it's meaning is dependant upon dcs value
        if ((self.dcs >> 2) & 3) == 0: # 7-bit, Default alphabet
            i = ((self.udl * 7) / 8) << 1
            if (self.udl * 7) % 8:
                i += 2
            self.message = self.convertGSM7bitToAscii(data[20:20 + i])
        elif ((self.dcs >> 2) & 3) == 1: # 8-bit data, binary
            self.message = "ERROR: Don't understand 8-bit binary messages"
        elif ((self.dcs >> 2) & 3) == 2: # 16-bit, UCS2 oh hell!  :)
            self.message = "ERROR: Don't understand 16-bit UCS2 messages"
        else:
            self.message = "ERROR: Don't understand this message format"

    def smsToData(self, date, number, smsc, message):
        # 0107911614910900F504 0B911614836816F1 0000 2050107034146B

        # add message type, sms-c details and reply path indicator
        self.timestamp = date
        self.number = number
        self.smsc = smsc
        self.message = message
        
        smsc = StringToGSMPhoneNumber(smsc)
        i = len(smsc) >> 1
        if i > 0:
            data = "01%s%s04" % (padFrontOfString(hex(i)[2:], 2), smsc)
        else:
            data = "010004"

        # add originating address
        if number[0] == '+':
            i = len(number) - 1
        else:
            i = len(number)
        number = StringToGSMPhoneNumber(number)
        data += "%s%s" % (padFrontOfString(hex(i)[2:], 2), number)

        # add PID, DCS
        data += "%s%s" % (padFrontOfString(hex(self.pid)[2:],2), padFrontOfString(hex(self.dcs)[2:],2))

        # add timestamp
        data += self.convertDateToTimestamp(date)

        # add UDL
        data += padFrontOfString(hex(len(message))[2:],2)

        # add the message (encoded in 7-bit GSM)
        self.rawMessage = data + self.convertAsciiToGSM7bit(message)

    def convertGSM7bitToAscii(self, data):
        i = 0
        mask = 0x7F
        last = 0
        res = []
        for c in unhexlify(data):
            # baaaaaaa ccbbbbbb dddccccc eeeedddd fffffeee ggggggff hhhhhhhg 0iiiiiii
            # 0aaaaaaa 0bbbbbbb 0ccccccc 0ddddddd 0eeeeeee 0fffffff 0ggggggg 0hhhhhhh 0iiiiiii
            val = ((ord(c) & mask) << i) + (last >> (8-i))
            res.append(chr(val))

            i += 1
            mask >>= 1
            last = ord(c)
            if i % 7 == 0:
                res.append(chr(last >> 1))
                i = 0
                mask = 0x7F
                last = 0
        return GSM3_38ToASCII(''.join(res))

    def convertAsciiToGSM7bit(self, data):
        i = 0
        l = 0
        mask = 0x0
        data = ASCIIToGSM3_38(data)
        res = []

        while l < len(data):
            # 0aaaaaaa 0bbbbbbb 0ccccccc 0ddddddd 0eeeeeee 0fffffff 0ggggggg 0hhhhhhh 0iiiiiii
            # baaaaaaa ccbbbbbb dddccccc eeeedddd fffffeee ggggggff hhhhhhhg 0iiiiiii
            c = ord(data[l])
            if i:
                res[-1] = chr(ord(res[-1]) + ((c & mask) << (8 - i)))
            if i != 7:
                res.append(chr(c >> i))

            i += 1
            mask = (mask << 1) + 1
            if i % 8 == 0:
                i = 0
                mask = 0x0
            l += 1

        return hexlify(''.join(res))

    def convertTimestamp(self, ts):
        # 2050107034146B
        self.timetuple = [0,0,0,0,0,0,0,0,0]

        self.timetuple[0] = int(ts[0]) + int(ts[1]) * 10
        if self.timetuple[0] >= 80:
            # Convert to previous century, hopefully no one uses this after 2079 ;)
            self.timetuple[0] += 1900
        else:
            # Convert to current century
            self.timetuple[0] += 2000

        #~ print ts
        self.timetuple[1] = int(ts[2]) + int(ts[3]) * 10
        self.timetuple[2] = int(ts[4]) + int(ts[5]) * 10
        self.timetuple[3] = int(ts[6]) + int(ts[7]) * 10
        self.timetuple[4] = int(ts[8]) + int(ts[9]) * 10
        self.timetuple[5] = int(ts[10]) + int(ts[11]) * 10
        self.timetuple[6] = calendar.weekday(self.timetuple[0], self.timetuple[1], self.timetuple[2])

        return time.asctime(self.timetuple)


    def convertDateToTimestamp(self, date):
        # Mon May 01 07:43:41 2002
        if not date:
            self.timetuple = time.localtime()
        else:
            self.timetuple = strptime(date)

        ts = ''
        for i in range(0, 6):
            s = ("%2d" % (self.timetuple[i])).replace(' ', '0')[-2:]
            ts += "%s%s" % (s[1], s[0])

        return ts + '00'

abbrevMonthNames = { "Jan":'01', "Feb":'02', "Mar":'03', "Apr":'04', "May":'05', "Jun":'06', "Jul":'07', "Aug":'08', "Sep":'09', "Oct":'10', "Nov":'11', "Dec":'12' }

def strptime(date):
    """Convert the date string into a 9 tuple"""
    df = [0,0,0,0,0,0,0,0,0]
    sp  = date.split(' ')
    spt = sp[3].split(':')

    df[0] = int(sp[4])      # Year
    if abbrevMonthNames.has_key(sp[1]):
        df[1] = int(abbrevMonthNames[sp[1]])
    else:
        df[1] = 1           # Month
    df[2] = int(sp[2])      # Day
    df[3] = int(spt[0])     # Hour
    df[4] = int(spt[1])     # Minute
    df[5] = int(spt[2])     # Second
    df[6] = calendar.weekday(df[0], df[1], df[2])

    return df
