#
# Script for looking/determining MCC, MNC, Carrier/Provider
# from a IMSI code
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

import json
from pprint import pprint

MCC_MNC_FILE = "scrapper/mcc_mnc.json"


def lookup_imsi(imsi):
    with open(MCC_MNC_FILE) as data_file:
        data = json.load(data_file)

        mcc = imsi[0:3]
        mnc3 = imsi[3:6]
        mnc2 = imsi[3:5]

        # check for 3 digit mnc first
        for item in data:
            if item["mcc"] == mcc and item["mnc"] == mnc3:
                return item

        # check for 2 digit mnc later
        for item in data:
            if item["mcc"] == mcc and item["mnc"] == mnc2:
                return item


if __name__ == '__main__':
    # quick internal tests

    # Softbank Japan
    print lookup_imsi("440201017068388")
