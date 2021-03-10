/*
 * Copyright 2018 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.ip4s

import scala.quoted._
import scala.util.Try

extension (inline ctx: StringContext)
  inline def ip (inline args: Any*): IpAddress =
    ${Literals.ip('ctx, 'args)}

  inline def ipv4 (inline args: Any*): Ipv4Address =
    ${Literals.ipv4('ctx, 'args)}

  inline def ipv6 (inline args: Any*): Ipv6Address =
    ${Literals.ipv6('ctx, 'args)}

  inline def mip (inline args: Any*): Multicast[IpAddress] =
    ${Literals.mip('ctx, 'args)}

  inline def mipv4 (inline args: Any*): Multicast[Ipv4Address] =
    ${Literals.mipv4('ctx, 'args)}

  inline def mipv6 (inline args: Any*): Multicast[Ipv6Address] =
    ${Literals.mipv6('ctx, 'args)}

  inline def ssmip (inline args: Any*): SourceSpecificMulticast[IpAddress] =
    ${Literals.ssmip('ctx, 'args)}

  inline def ssmipv4 (inline args: Any*): SourceSpecificMulticast[Ipv4Address] =
    ${Literals.ssmipv4('ctx, 'args)}

  inline def ssmipv6 (inline args: Any*): SourceSpecificMulticast[Ipv6Address] =
    ${Literals.ssmipv6('ctx, 'args)}

  inline def port (inline args: Any*): Port =
    ${Literals.port('ctx, 'args)}

  inline def host (inline args: Any*): Hostname =
    ${Literals.host('ctx, 'args)}

  inline def idn (inline args: Any*): IDN =
    ${Literals.idn('ctx, 'args)}

object Literals:

  trait Validator[A]:
    def validate(s: String): Option[String]
    def build(s: String)(using Quotes): Expr[A]

    def apply(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] =
      strCtxExpr.value match
        case Some(sc) => apply(sc.parts, argsExpr)
        case None =>
          quotes.reflect.report.error("StringContext args must be statically known")
          ???

    private def apply(parts: Seq[String], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] =
      if parts.size == 1 then
        val literal = parts.head
        validate(literal) match
          case Some(err) =>
            quotes.reflect.report.error(err)
            ???
          case None =>
            build(literal)
      else
        quotes.reflect.report.error("interpolation not supported", argsExpr)
        ???

  object ip extends Validator[IpAddress]:
    def validate(s: String): Option[String] =
      IpAddress.fromString(s).fold(Some("Invalid IP address"))(_ => None)
    def build(s: String)(using Quotes): Expr[IpAddress] =
      '{_root_.com.comcast.ip4s.IpAddress.fromString(${Expr(s)}).get}

  object ipv4 extends Validator[Ipv4Address]:
    def validate(s: String): Option[String] =
      Ipv4Address.fromString(s).fold(Some("Invalid IPv4 address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Ipv4Address] =
      '{_root_.com.comcast.ip4s.Ipv4Address.fromString(${Expr(s)}).get}

  object ipv6 extends Validator[Ipv6Address]:
    def validate(s: String): Option[String] =
      Ipv6Address.fromString(s).fold(Some("Invalid IPv6 address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Ipv6Address] =
      '{_root_.com.comcast.ip4s.Ipv6Address.fromString(${Expr(s)}).get}

  object mip extends Validator[Multicast[IpAddress]]:
    def validate(s: String): Option[String] =
      IpAddress.fromString(s).flatMap(_.asMulticast).fold(Some("Invalid IP multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[IpAddress]] =
      '{_root_.com.comcast.ip4s.IpAddress.fromString(${Expr(s)}).get.asMulticast.get}

  object mipv4 extends Validator[Multicast[Ipv4Address]]:
    def validate(s: String): Option[String] =
      Ipv4Address.fromString(s).flatMap(_.asMulticast).fold(Some("Invalid IPv4 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[Ipv4Address]] =
      '{_root_.com.comcast.ip4s.Ipv4Address.fromString(${Expr(s)}).get.asMulticast.get}

  object mipv6 extends Validator[Multicast[Ipv6Address]]:
    def validate(s: String): Option[String] =
      Ipv6Address.fromString(s).flatMap(_.asMulticast).fold(Some("Invalid IPv6 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[Ipv6Address]] =
      '{_root_.com.comcast.ip4s.Ipv6Address.fromString(${Expr(s)}).get.asMulticast.get}

  object ssmip extends Validator[SourceSpecificMulticast[IpAddress]]:
    def validate(s: String): Option[String] =
      IpAddress.fromString(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IP multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[IpAddress]] =
      '{_root_.com.comcast.ip4s.IpAddress.fromString(${Expr(s)}).get.asSourceSpecificMulticast.get}

  object ssmipv4 extends Validator[SourceSpecificMulticast[Ipv4Address]]:
    def validate(s: String): Option[String] =
      Ipv4Address.fromString(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IPv4 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[Ipv4Address]] =
      '{_root_.com.comcast.ip4s.Ipv4Address.fromString(${Expr(s)}).get.asSourceSpecificMulticast.get}

  object ssmipv6 extends Validator[SourceSpecificMulticast[Ipv6Address]]:
    def validate(s: String): Option[String] =
      Ipv6Address.fromString(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IPv6 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[Ipv6Address]] =
      '{_root_.com.comcast.ip4s.Ipv6Address.fromString(${Expr(s)}).get.asSourceSpecificMulticast.get}

  object port extends Validator[Port]:
    def validate(s: String): Option[String] =
      Try(s.toInt).toOption.flatMap(Port.fromInt).fold(Some("Invalid port"))(_ => None)
    def build(s: String)(using Quotes): Expr[Port] =
      '{_root_.com.comcast.ip4s.Port.fromInt(${Expr(s.toInt)}).get}

  object host extends Validator[Hostname]:
    def validate(s: String): Option[String] =
      Hostname.fromString(s).fold(Some("Invalid hostname"))(_ => None)
    def build(s: String)(using Quotes): Expr[Hostname] =
      '{_root_.com.comcast.ip4s.Hostname.fromString(${Expr(s)}).get}

  object idn extends Validator[IDN]:
    def validate(s: String): Option[String] =
      IDN.fromString(s).fold(Some("Invalid IDN"))(_ => None)
    def build(s: String)(using Quotes): Expr[IDN] =
      '{_root_.com.comcast.ip4s.IDN.fromString(${Expr(s)}).get}
