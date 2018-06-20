<!--
# Copyright (C) 2018 SRC Solution.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     SRC Solution - Pilot Things / GFA - initial API and implementation
-->

# agile-om2m
An om2m java protocol implementation for AGILE gateway

# Description
This module is composed by the following elements:
*The oM2M server which implements the oneM2M standard. The files are located in the om2m/ directory
*The DALI connector (IPE) library that allow the gateway to interact with DALI protocol through Tridonic USB-DALI adapter. You can find it in the directory om2m/plugins/ipe-dali-1.2.1-SNAPSHOT.jar
*The agile-om2m protocol itself. The files are located in the org.eclipse.agail.protocol.ONEM2M/ directory
*The other .jar files in the root allow the agile protocol to interact with the oM2M server.

# How to use
You need to follow the steps described in the agile-dev and agile-stack repository.

The agile-om2m should either be inside the protocol directory of the modules of agile-dev or in the apps directory of the agile-stack module.

Then you will have to add the agile-om2m protocol in the docker compose file and build it.

Before running the agile-om2m container, ensure that you connected the Tridonic USB-DALI adapter first. If you do not do it first, it could not work properly.