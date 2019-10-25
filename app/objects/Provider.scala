// Webside Source Code Zauberstuhl.de
// Copyright (C) 2016-2019  Lukas Matt <lukas@matt.wf>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
package objects

object Provider {
  trait Provider {
    def name: String = getClass.getName
  }
  case class StripeProvider() extends Provider
  case class BlockChainProvider() extends Provider
  case class EmailProvider() extends Provider
}
