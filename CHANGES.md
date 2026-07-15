# `laaws-soap-service` Release Notes

## 1.6.0 (LOCKSS 2.0.91-beta2)

### API Changes

* Consolidated the `/aus/{auid}/artifacts` endpoint into `/artifacts`; moved AUID to a query parameter
* Propagated new `IncludeContentEnum` throughout the codebase
* Updated `PageInfo` spec: some properties now nullable; renamed `resultsPerPage` to `itemsInPage`

### Features

* Merged MDQ and MDX services into a single MD service

### Configuration

* Adopted 2.0-beta2 port conventions
