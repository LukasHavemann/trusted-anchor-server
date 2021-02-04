# Trusted anchor server

Trusted anchor is a [RFC-3161](https://www.ietf.org/rfc/rfc3161.txt) compatible notarisation service for audit logs.
Trusted anchor provides a REST interface for the creation of RFC-3161 timestamp to prove existence of events to third parties. 
Event hashes are recorded in rounds and stored in a history tree. The history tree itself or the root hash can be published in
a public medium like a blockchain to prevent tampering of event data or timestamps.

This is a prototyp implementation for my masterthesis
at [Nordakaemie Graduate School](https://www.nordakademie.de/graduate-school). The loadtest driver can be found
under [trusted-anchor-loadtest-driver](https://github.com/LukasHavemann/trusted-anchor-loadtest-driver).
