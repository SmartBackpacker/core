package com.github.gvolpe.smartbackpacker.http.auth

import tsec.mac.imports.{HMACSHA256, MacSigningKey}

object config {

  private val AccessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJQbGVhc2VDaGFuZ2VNZTRzUzAwbkFzUDBzczFibDMhOikiLCJpYXQiOjE1MTE5NzIyMzksImp0aSI6IjYyYjVkYWFjZDFiMjUzNmJkN2ZmYjBiNzI4Y2UxZDI2ZjliZGRkMmZmYzI5Y2FkNzZkNTdkZGM3MDBkZjc5ZWIifQ.IGAj_AFBjPzU54A2qezWixq2jd87kS02A4b4H4IRUco"

  case class AuthConfig(jwtKey: MacSigningKey[HMACSHA256] = HMACSHA256.buildKeyUnsafe(AccessToken.getBytes))

  case class TokenRequest(clientId: String, code: String)

}
