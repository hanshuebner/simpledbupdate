# Simple DB Update
A simple database update mechanism that takes SQL scripts and updates your database. A very simple approach without
any fancy gadgets.

It can be used as an Ant task or in similar build scripts.

## What problem does it solve?
Most (web)applications come with a (relational) database. Today its common practice to release often, sometimes every
day. Therefor the release mechanism should be as painless as possible.

Continuous build servers such as Hudson / Jenkis do a great job here. But what about the database? The structure of
the database and the code version must match. This is what Simple DB Update does.

## What is Simple DB Update?
Simple DB Update consists only of a few Java classes. Feel free to take and adapt them for your project (they are
licensed under the Apache License 2.0)

## How does it work?
Simple DB Update looks in a folder for SQL scripts. If it finds one (or more) then it executes them in the given data
base. It keeps track of the executes scripts in a version table.
