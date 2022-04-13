{{/*
Create a default fully qualified app name.
We truncate at 63 characters because some kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ems.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 characters because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "common.labels" -}}
helm.sh/chart: {{ include "ems.chart" . }}
app.kubernetes.io/version: {{ .Values.emsVersion }}
app.kubernetes.io/part-of: {{ include "fullname" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
tibco.com/technology: ems
tibco.com/group: infrastructure
{{- end }}

{{/*
EMS Server labels
*/}}
{{- define "ems.labels" -}}
{{ include "common.labels" . }}
app.kubernetes.io/name: {{ .Values.name }}
app.kubernetes.io/component: server
{{ include "ems.selectorLabels" . }}
{{- end }}

{{/*
EMS Server selector labels
*/}}
{{- define "ems.selectorLabels" -}}
app: {{ template "name" . }}
environment: {{ .Values.environment }}
{{- end -}}

{{/*
EMS Client labels
*/}}
{{- define "emsClient.labels" -}}
{{ include "common.labels" . }}
app.kubernetes.io/name: {{ .Values.name }}-client
app.kubernetes.io/component: client
{{ include "emsClient.selectorLabels" . }}
{{- end }}

{{/*
EMS Client selector labels
*/}}
{{- define "emsClient.selectorLabels" -}}
app: {{ template "name" . }}-client
environment: {{ .Values.environment }}
{{- end -}}