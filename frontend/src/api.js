export async function runCode(language, code) {
  const response = await fetch("http://localhost:8080/api/code/run", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      language,
      code,
    }),
  });

  return response.json();
}
