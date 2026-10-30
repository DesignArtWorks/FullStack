"""Validate Surefire XML, including tests aggregated from JUnit @Nested classes."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def validate_reports(directory: Path) -> tuple[int, int]:
    # 132-ci-integration-e2e:FR-001/FR-002: fail closed without treating empty TXT
    # container summaries as empty suites; Surefire XML aggregates nested tests.
    reports = sorted(directory.glob("TEST-com.escala.authservice.integration.*IntegrationTest.xml"))
    if not reports:
        raise ValueError("No integration XML report was produced.")
    total = 0
    for report in reports:
        try:
            suite = ET.parse(report).getroot()
            counts = {key: int(suite.attrib[key]) for key in ("tests", "failures", "errors", "skipped")}
        except (ET.ParseError, KeyError, ValueError) as error:
            raise ValueError(f"Invalid integration report: {report}: {error}") from error
        cases = suite.findall("testcase")
        if (suite.tag != "testsuite" or counts["tests"] <= 0
                or any(counts[key] != 0 for key in ("failures", "errors", "skipped"))
                or len(cases) != counts["tests"]
                or any(case.find(tag) is not None for case in cases for tag in ("failure", "error", "skipped"))):
            raise ValueError(f"Integration report is skipped, empty or unsuccessful: {report}")
        total += counts["tests"]
    return len(reports), total


if __name__ == "__main__":
    try:
        report_count, test_count = validate_reports(Path(sys.argv[1]))
    except (ValueError, OSError, IndexError) as error:
        sys.exit(str(error))
    print(f"Integration XML: {report_count} suites, {test_count} tests, no failures/errors/skips.")
