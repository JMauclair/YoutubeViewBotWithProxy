namespace OpenQA.Selenium.DevTools.Emulation
{
    using Newtonsoft.Json;

    /// <summary>
    /// Clears the overriden device metrics.
    /// </summary>
    public sealed class ClearDeviceMetricsOverrideCommandSettings : ICommand
    {
        private const string DevToolsRemoteInterface_CommandName = "Emulation.clearDeviceMetricsOverride";
        
        [JsonIgnore]
        public string CommandName
        {
            get { return DevToolsRemoteInterface_CommandName; }
        }

    }

    public sealed class ClearDeviceMetricsOverrideCommandResponse : ICommandResponse<ClearDeviceMetricsOverrideCommandSettings>
    {
    }
}