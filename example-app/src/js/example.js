import { tools } from 'fantastic-capacitor-plugin';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    tools.echo({ value: inputValue })
}
