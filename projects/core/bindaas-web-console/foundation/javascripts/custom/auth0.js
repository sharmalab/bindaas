let auth0 = null;

//FIXME read values from file
const configureClient = async () => {
    auth0 = await createAuth0Client({
        domain: 'tushar-97.auth0.com',
        client_id: 'RHWrWpw9Yf0SgGOBlpG0mXqIwIEhykN4',
        audience: 'my-api',
        prompt: 'login'
    });
};


window.onload = async () => {
    await configureClient();
    document.getElementById("inputButton").disabled = false;

};


const login = async () => {

    document.getElementById('inputButton').style.opacity=0.5;

    const token = await auth0.getTokenWithPopup();
    const user = await auth0.getUser();

    document.getElementById("accessToken").value = token;

    auth0.logout();

    document.getElementById("auth0-form").submit();

};


