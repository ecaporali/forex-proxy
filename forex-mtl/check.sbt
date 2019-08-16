addCommandAlias("check", ";clean;coverage;test;coverageReport")

coverageMinimum := 95
coverageFailOnMinimum := true //fail build if coverage falls below coverageMinimum
coverageExcludedPackages := """
  ;forex.config.*
  ;forex.services.rates.Interpreters
  ;forex.*.package.*
  ;forex.Module.*
  ;forex.Main.*
  ;forex.Application.*
"""
