#!/usr/bin/env python

#
# Utility to display some informations about a SIM card
#
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
from constants import *
from binascii import hexlify, unhexlify
import traceback


class SimTester():

    def __init__(self, device, baudrate):

        # create trasnport
	self.sl = SerialSimLink(device=device, baudrate=baudrate)

        # wait for SIM card
        print("[SimTester] Waiting for SIM card ...")
        self.sl.wait_for_card()

        # program the card
        print("[SimTester] Reading SIM card ...")


if __name__ == '__main__':
    device="/dev/ttyUSB0"
    baudrate = 9600
    tester = SimTester(device='/dev/ttyUSB0', baudrate=9600)
