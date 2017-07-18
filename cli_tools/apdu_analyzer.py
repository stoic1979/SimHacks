#
# Script for analyzing APDU, Status Words etc.
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


class ApduAnalyzer:

    # check for both SW1 and SW2
    sw1sw2_defs = {
            "9000": "Command successfull",

            "6F00": "Unknown result",
            "6f00": "Unknown result",

            "6D00": "Command not allowed (invalid/unknown/unauthorized instruction)",
            "6d00": "Command not allowed (invalid/unknown/unauthorized instruction)",

            "6A82": "File not found",
            "6a82": "File not found",

            "6A81": "Function not supported",
            "6a81": "Function not supported",

            "6283": "Selected file is deactivated",

            }

    def __init__(self):
        pass

    def check_status_words(self, sw1, sw2):
        status = sw1 + sw2

        if self.sw1sw2_defs.has_key(status):
            return self.sw1sw2_defs[status]

        if sw1 == "67":
            return "Length fields wrong, cannot recover, response lost"

        if sw1 == "61":
            return "More ('%s' bytes) data is remaining to be read" % sw2

        if sw1 == "6C" or sw1 == "6c":
            return "Le not accepted. Actual available response length is called" \
            "'La' and is contained in '%s'. Command can be re-issued" \
            "in order to retrieve the data." % sw2

        if sw1 == "91":
            return "Command successful, there are also proactive commands" \
                "awaiting execution (ME should use FETCH)."

        if sw1 == "92":
            return "Various memory errors"

        if sw1 == "94":
            return "Various file errors"

        if sw1 == "98":
            return "Various security errors"

        if sw1 == "9F" or sw1 == "9f":
            return "Success, '%s' bytes available to be read with 'Get Data'." % sw2




if __name__ == "__main__":
    anr = ApduAnalyzer()
    print anr.check_status_words('90', '00')
    print anr.check_status_words('62', '83')
    print anr.check_status_words('6c', '83')
    print anr.check_status_words('91', 'aa')
    print anr.check_status_words('92', '12')
    print anr.check_status_words('94', 'aa')
    print anr.check_status_words('98', 'da')
    print anr.check_status_words('9f', '82')

