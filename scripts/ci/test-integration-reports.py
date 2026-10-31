"""Regression controls for 132-ci-integration-e2e:FR-001/FR-002."""

import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("reports", Path(__file__).with_name("check-integration-reports.py"))
reports = importlib.util.module_from_spec(spec)
spec.loader.exec_module(reports)


class IntegrationReportTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)

    def write_report(self, content, name="AuthHttpIntegrationTest"):
        (self.directory / f"TEST-com.escala.authservice.integration.{name}.xml").write_text(content, encoding="utf-8")

    def suite(self, tests=1, failures=0, errors=0, skipped=0, cases='<testcase name="nested" classname="Authenticate"/>'):
        return f'<testsuite tests="{tests}" failures="{failures}" errors="{errors}" skipped="{skipped}">{cases}</testsuite>'

    def test_nested_xml_passes_despite_empty_parent_text_summary(self):
        self.write_report(self.suite())
        (self.directory / "com.escala.authservice.integration.AuthHttpIntegrationTest.txt").write_text("Tests run: 0, Failures: 0, Errors: 0, Skipped: 0")
        self.assertEqual(reports.validate_reports(self.directory), (1, 1))

    def test_missing_reports_fail(self):
        with self.assertRaises(ValueError):
            reports.validate_reports(self.directory)

    def test_empty_failed_errored_and_skipped_suites_fail(self):
        for counts in ({"tests": 0, "cases": ""}, {"failures": 1}, {"errors": 1}, {"skipped": 1}):
            with self.subTest(counts=counts):
                self.write_report(self.suite(**counts))
                with self.assertRaises(ValueError):
                    reports.validate_reports(self.directory)

    def test_invalid_or_incomplete_xml_fails(self):
        for content in ("<testsuite", "<testsuite/>", '<testsuite tests="bad"/>'):
            with self.subTest(content=content):
                self.write_report(content)
                with self.assertRaises(ValueError):
                    reports.validate_reports(self.directory)

    def test_inconsistent_case_count_fails(self):
        self.write_report(self.suite(tests=2))
        with self.assertRaises(ValueError):
            reports.validate_reports(self.directory)

    def test_hidden_case_failure_error_or_skip_fails(self):
        for tag in ("failure", "error", "skipped"):
            with self.subTest(tag=tag):
                self.write_report(self.suite(cases=f'<testcase><{tag}/></testcase>'))
                with self.assertRaises(ValueError):
                    reports.validate_reports(self.directory)

    def test_all_suites_must_pass(self):
        self.write_report(self.suite())
        self.write_report(self.suite(skipped=1), "OtherIntegrationTest")
        with self.assertRaises(ValueError):
            reports.validate_reports(self.directory)

    def test_non_integration_reports_do_not_satisfy_gate(self):
        (self.directory / "TEST-UnitTest.xml").write_text(self.suite())
        with self.assertRaises(ValueError):
            reports.validate_reports(self.directory)


if __name__ == "__main__":
    unittest.main()
