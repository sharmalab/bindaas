let auth0 = null;

const configureClient = async (auth0Domain,auth0ClientId,auth0Audience) => {
    auth0 = await createAuth0Client({
        domain: auth0Domain,
        client_id: auth0ClientId,
        audience: auth0Audience,
        prompt: 'login'
    });
};


window.onload = async () => {

    let auth0Domain = document.getElementById("domain").value;
    let auth0ClientId = document.getElementById("clientId").value;
    let auth0Audience = document.getElementById("audience").value;

    await configureClient(auth0Domain,auth0ClientId,auth0Audience);
    document.getElementById("inputButton").disabled = false;

};


const login = async () => {

    document.getElementById("inputButton").disabled=true;

    const token = await auth0.getTokenWithPopup();
    const user = await auth0.getUser();

    document.getElementById("accessToken").value = token;

    auth0.logout();

    document.getElementById("inputButton").disabled=false;

    document.getElementById("auth0-form").submit();

};


