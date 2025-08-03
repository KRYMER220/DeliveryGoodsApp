package ru.krymer.delivery


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.ok.tracer.CoreTracerConfiguration
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import ru.ok.tracer.disk.usage.DiskUsageConfiguration
import ru.ok.tracer.heap.dumps.HeapDumpConfiguration
import ru.ok.tracer.profiler.sampling.SamplingProfilerConfiguration
import ru.ok.tracer.profiler.systrace.SystraceProfilerConfiguration

@HiltAndroidApp
class AppBox : Application(), HasTracerConfiguration {
    override val tracerConfiguration: List<TracerConfiguration>
        get() = listOf(
            CoreTracerConfiguration.build {},
            CrashReportConfiguration.build {},
            CrashFreeConfiguration.build {},
            HeapDumpConfiguration.build {},
            DiskUsageConfiguration.build {},
            SystraceProfilerConfiguration.build {},
            SamplingProfilerConfiguration.build {},
        )
}