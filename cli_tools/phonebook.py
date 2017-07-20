#!/usr/bin/env python

#
# Phonebook to display contacts and SMS of a SIM card
#
# Copyright (C) 2017  Navjot Singh <weavebytes@gmail.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.


from transport.serial import SerialSimLink
from utils import *

from utils import *

from binascii import hexlify, unhexlify

from sms import SMSmessage
import traceback


class Phonebook():

    def __init__(self, device, baudrate):

        # create trasnport
	self.sl = SerialSimLink(device=device, baudrate=baudrate)

        # wait for SIM card
        print("[Phonebook] Waiting for SIM card ...")
        self.sl.wait_for_card()

        # program the card
        print("[Phonebook] Reading SIM card ...")

    def send_apdu_list(self, lst):
        for apdu in lst:
            print ("In: %s" % apdu)
            out, sw = self.sl.send_apdu_raw(apdu)
            print "SW:", sw
            print "OUT:", out
            print

    def send_apdu_list_prefixed(self, lst):
        lst = ["A0A4000002" + l for l in lst]
        print ("Prefixed list: %s" % lst)
        self.send_apdu_list(lst)

    def connect_to_sim(self):
        connect_sim_apdu_lst = ['A0A40000023F00', 'A0F200000D', 'A0F2000016']
        print ("Connecting to SIM...")
        ret = self.send_apdu_list(connect_sim_apdu_lst)
        print ("Connected")
        print

    def get_contacts(self):

        phone_lst = []

        print ("Selecting file")
        self.send_apdu_list_prefixed(['3F00', '7F10', '6F3A'])

        data, sw = self.sl.send_apdu_raw("A0C000000F")

        rec_len  = int(data[28:30], 16) # Usually 0x20

        # Now we can work out the name length & number of records
        name_len = rec_len - 14 # Defined GSM 11.11
        num_recs = int(data[4:8], 16) / rec_len

        print ("rec_len: %d, name_len: %d, num_recs: %d" % (rec_len, name_len, num_recs))


        apdu_str = "A0B2%s04" + IntToHex(rec_len)
        hexNameLen = name_len << 1

        try:
            for i in range(1, num_recs + 1):
                apdu = apdu_str % IntToHex(i)
                data, sw = self.sl.send_apdu_raw(apdu)

                print ("Contact #%d" % i)
                print ("In: %s" % apdu)
                print "SW:", sw
                print "OUT:", data

                if data[0:2] != 'FF':
                    name = GSM3_38ToASCII(unhexlify(data[:hexNameLen]))
                    if ord(name[-1]) > 0x80:
                        # Nokia phones add this as a group identifier. Remove it.
                        name = name[:-1].rstrip()
                    number = ""

                    numberLen = int(data[hexNameLen:hexNameLen+2], 16)
                    if numberLen > 0 and numberLen <= (11): # Includes TON/NPI byte
                        hexNumber = data[hexNameLen+2:hexNameLen+2+(numberLen<<1)]
                        if hexNumber[:2] == '91':
                            number = "+"
                        number += GSMPhoneNumberToString(hexNumber[2:])
                    #self.itemDataMap[i] = (name, number)
                    print "Name: ", name
                    print "Number: ", number
                    phone_lst.append((name, number))

                print
        except Exception as exp:
            print "\n\nget_phonebook() got exception :: %s\n\n" % exp

        return phone_lst

    def get_sms(self):

        sms_lst = []

        print ("Selecting SMS file")
        self.send_apdu_list_prefixed(['3F00', '7F10', '6F3C'])

        data, sw = self.sl.send_apdu_raw("A0C000000F")

        rec_len = int(data[28:30], 16) # Should be 0xB0 (176)
        num_recs = int(data[4:8], 16) / rec_len

        print ("rec_len: %d, num_recs: %d" % (rec_len, num_recs))

        apdu_str = "A0B2%s04" + IntToHex(rec_len)

        try:
            for i in range(1, num_recs + 1):
                apdu = apdu_str % IntToHex(i)
                data, sw = self.sl.send_apdu_raw(apdu)

                print ("SMS #%d" % i)
                print ("In: %s" % apdu)
                print "SW:", sw
                print "OUT:", data
                print

                # See if SMS record is used
                status = int(data[0:2], 16)
                if status & 1 or data[2:4] != 'FF':
                    try:
                        sms = SMSmessage()
                        sms.smsFromData(data)
                        sms_lst.append( (sms.status, sms.timestamp, sms.number, sms.message) )
                    except Exception as exp:
                        pass
                        #print "\n\nget_sms() got exception: %s\n while fetching SMS from data, for SMS #%d\n\n" % (exp, i)
                        #print traceback.format_exc()
        except Exception as exp:
            print "\n\nget_sms() got exception :: %s\n\n" % exp
            print traceback.format_exc()

        return sms_lst


if __name__ == '__main__':
    pb = Phonebook(device='/dev/ttyUSB0', baudrate=9600)

    print pb.get_contacts()
    print pb.get_sms()

