import { useState } from "react";
import { runCode } from "./api";

function App() {
  const [language, setLanguage] = useState("java");

  const [code, setCode] = useState(
    `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Docker!");
    }
}`
  );

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleRun = async () => {
    setLoading(true);
    setResult(null);

    try {
      const data = await runCode(language, code);
      setResult(data);
    } catch (error) {
      setResult({
        success: false,
        output: "",
        error: "Unable to connect to backend",
        memoryUsed: null,
        executionTime: null,
      });
    } finally {
      setLoading(false);
    }
  };

  const getStatus = () => {
    if (!result) {
      return "—";
    }

    if (result.success) {
      return "Success";
    }

    if (result.error === "Execution timed out") {
      return "Timed Out";
    }

    return "Failed";
  };

  return (
    <div className="app">
      <h1>Online Code Editor</h1>

      <div className="toolbar">
        <select
          value={language}
          onChange={(e) => setLanguage(e.target.value)}
        >
          <option value="java">Java</option>
        </select>

        <button onClick={handleRun} disabled={loading}>
          {loading ? "Running..." : "Run Code"}
        </button>
      </div>

      <textarea
        value={code}
        onChange={(e) => setCode(e.target.value)}
        spellCheck="false"
      />

      <section className="result">
        <h2>Execution Result</h2>

        <p>
          <strong>Status:</strong> {getStatus()}
        </p>

        <p>
          <strong>Execution Time:</strong>{" "}
          {result?.executionTime || "—"}
        </p>

        <p>
          <strong>Peak Memory Used:</strong>{" "}
          {result?.memoryUsed || "—"}
        </p>

        <h3>Output</h3>
        <pre>{result?.output || "—"}</pre>

        <h3>Error</h3>
        <pre>{result?.error || "—"}</pre>
      </section>
    </div>
  );
}

export default App;
